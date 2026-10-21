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


package io.fireflysoftware.domain.common.sca.core.services;

import io.fireflysoftware.common.sca.sdk.model.PaginationResponseSCAAttemptDTO;
import io.fireflysoftware.common.sca.sdk.model.PaginationResponseSCAOperationHistoryDTO;
import io.fireflysoftware.common.sca.sdk.model.SCAChallengeDTO;
import io.fireflysoftware.common.sca.sdk.model.SCAOperationDTO;
import io.fireflysoftware.domain.common.sca.core.commands.ChallengeType;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Domain service for Strong Customer Authentication operations.
 */
public interface ScaService {

    /**
     * Initiates a new SCA operation and generates + delivers its first challenge.
     */
    Mono<SagaResult> initiateOperation(String operationType, UUID resourceId, String channel);

    /**
     * Generates (or regenerates) a challenge for an existing SCA operation.
     */
    Mono<SCAChallengeDTO> generateChallenge(UUID operationId, ChallengeType challengeType);

    /**
     * Verifies a user's attempt, updates the operation status, and records an audit event.
     */
    Mono<SagaResult> verifyAttempt(UUID operationId, UUID challengeId, String verificationCode);

    /**
     * Retrieves a single SCA operation by its identifier.
     */
    Mono<SCAOperationDTO> getOperation(UUID operationId);

    /**
     * Retrieves all attempts for the latest challenge on the given operation.
     */
    Mono<PaginationResponseSCAAttemptDTO> getAttempts(UUID operationId);

    /**
     * Retrieves the status-change history for a given SCA operation.
     */
    Mono<PaginationResponseSCAOperationHistoryDTO> getStatusHistory(UUID operationId);
}
