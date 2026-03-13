package com.firefly.domain.common.sca.core.services;

import com.firefly.common.sca.sdk.model.PaginationResponseSCAAttemptDTO;
import com.firefly.common.sca.sdk.model.PaginationResponseSCAOperationHistoryDTO;
import com.firefly.common.sca.sdk.model.SCAChallengeDTO;
import com.firefly.common.sca.sdk.model.SCAOperationDTO;
import com.firefly.domain.common.sca.core.commands.ChallengeType;
import com.firefly.domain.common.sca.core.commands.GenerateScaChallengeCommand;
import com.firefly.domain.common.sca.core.commands.InitiateScaOperationCommand;
import com.firefly.domain.common.sca.core.commands.VerifyScaAttemptCommand;
import com.firefly.domain.common.sca.core.queries.GetScaAttemptsQuery;
import com.firefly.domain.common.sca.core.queries.GetScaOperationQuery;
import com.firefly.domain.common.sca.core.queries.GetScaStatusHistoryQuery;
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
