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
import io.fireflysoftware.domain.common.sca.core.commands.GenerateScaChallengeCommand;
import io.fireflysoftware.domain.common.sca.core.commands.InitiateScaOperationCommand;
import io.fireflysoftware.domain.common.sca.core.commands.VerifyScaAttemptCommand;
import io.fireflysoftware.domain.common.sca.core.queries.GetScaAttemptsQuery;
import io.fireflysoftware.domain.common.sca.core.queries.GetScaOperationQuery;
import io.fireflysoftware.domain.common.sca.core.queries.GetScaStatusHistoryQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.cqrs.query.QueryBus;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Default implementation of {@link ScaService} — delegates to the CQRS bus.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScaServiceImpl implements ScaService {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    @Override
    public Mono<SagaResult> initiateOperation(String operationType, UUID resourceId, String channel) {
        return commandBus.send(new InitiateScaOperationCommand(operationType, resourceId, channel));
    }

    @Override
    public Mono<SCAChallengeDTO> generateChallenge(UUID operationId, ChallengeType challengeType) {
        return commandBus.send(new GenerateScaChallengeCommand(operationId, challengeType));
    }

    @Override
    public Mono<SagaResult> verifyAttempt(UUID operationId, UUID challengeId, String verificationCode) {
        return commandBus.send(new VerifyScaAttemptCommand(operationId, challengeId, verificationCode));
    }

    @Override
    public Mono<SCAOperationDTO> getOperation(UUID operationId) {
        return queryBus.query(new GetScaOperationQuery(operationId));
    }

    @Override
    public Mono<PaginationResponseSCAAttemptDTO> getAttempts(UUID operationId) {
        return queryBus.query(new GetScaAttemptsQuery(operationId));
    }

    @Override
    public Mono<PaginationResponseSCAOperationHistoryDTO> getStatusHistory(UUID operationId) {
        return queryBus.query(new GetScaStatusHistoryQuery(operationId));
    }
}
