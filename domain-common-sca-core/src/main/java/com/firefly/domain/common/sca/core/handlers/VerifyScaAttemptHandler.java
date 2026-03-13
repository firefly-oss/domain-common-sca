package com.firefly.domain.common.sca.core.handlers;

import com.firefly.domain.common.sca.core.commands.VerifyScaAttemptCommand;
import com.firefly.domain.common.sca.core.sagas.VerifyScaSaga;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import reactor.core.publisher.Mono;

/**
 * Handles {@link VerifyScaAttemptCommand} by dispatching the {@code verify-sca} saga.
 */
@Slf4j
@CommandHandlerComponent(timeout = 30000, metrics = true, tracing = true)
public class VerifyScaAttemptHandler extends CommandHandler<VerifyScaAttemptCommand, SagaResult> {

    private final SagaEngine sagaEngine;

    public VerifyScaAttemptHandler(SagaEngine sagaEngine) {
        this.sagaEngine = sagaEngine;
    }

    @Override
    protected Mono<SagaResult> doHandle(VerifyScaAttemptCommand command) {
        log.debug("Dispatching verify-sca saga for operationId={}", command.getOperationId());

        StepInputs inputs = StepInputs.of(VerifyScaSaga.STEP_VALIDATE_ATTEMPT, command);
        return sagaEngine.execute(VerifyScaSaga.SAGA_NAME, inputs);
    }
}
