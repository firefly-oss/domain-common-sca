/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fireflysoftware.domain.common.sca.challenge;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates a challenge, delivers it through {@link ChallengeDeliveryPort}, and validates the response.
 * This is not a password or biometric login.
 */
public final class ScaChallengeService {

    private final ChallengeDeliveryPort delivery;
    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<UUID, Pending> pending = new ConcurrentHashMap<>();

    public ScaChallengeService(ChallengeDeliveryPort delivery) {
        this.delivery = delivery;
    }

    public ChallengeDeliveryPort.Delivery create(UUID paymentId) {
        int code = 100000 + random.nextInt(900000);
        String value = Integer.toString(code);
        UUID challengeId = UUID.randomUUID();
        pending.put(challengeId, new Pending(paymentId, value, Instant.now().plusSeconds(300), ChallengeStatus.PENDING));
        return delivery.deliver(challengeId, value);
    }

    public ChallengeStatus validate(UUID challengeId, String response) {
        Pending current = pending.get(challengeId);
        if (current == null || Instant.now().isAfter(current.expiresAt())) {
            if (current != null) {
                pending.put(challengeId, current.withStatus(ChallengeStatus.EXPIRED));
            }
            return ChallengeStatus.EXPIRED;
        }
        if (response == null || !current.value().equals(response)) {
            pending.put(challengeId, current.withStatus(ChallengeStatus.FAILED));
            return ChallengeStatus.FAILED;
        }
        pending.put(challengeId, current.withStatus(ChallengeStatus.VALIDATED));
        return ChallengeStatus.VALIDATED;
    }

    public void expire(UUID challengeId) {
        Pending current = pending.get(challengeId);
        if (current != null) {
            pending.put(challengeId, current.withStatus(ChallengeStatus.EXPIRED));
        }
    }

    private record Pending(UUID paymentId, String value, Instant expiresAt, ChallengeStatus status) {
        Pending withStatus(ChallengeStatus next) {
            return new Pending(paymentId, value, expiresAt, next);
        }
    }
}
