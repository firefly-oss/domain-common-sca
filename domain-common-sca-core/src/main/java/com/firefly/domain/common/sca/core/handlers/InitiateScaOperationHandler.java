package com.firefly.domain.common.sca.core.handlers;

import com.firefly.domain.common.sca.core.commands.InitiateScaOperationCommand;
import com.firefly.domain.common.sca.core.sagas.InitiateScaSaga;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import reactor.core.publisher.Mono;

/**
 * Handles {@link InitiateScaOperationCommand} by dispatching the {@code initiate-sca} saga.
 */
@Slf4j
@CommandHandlerComponent(timeout = 30000, metrics = true, tracing = true)
public class InitiateScaOperationHandler extends CommandHandler<InitiateScaOperationCommand, SagaResult> {

    private final SagaEngine sagaEngine;

    public InitiateScaOperationHandler(SagaEngine sagaEngine) {
        this.sagaEngine = sagaEngine;
    }

    @Override
    protected Mono<SagaResult> doHandle(InitiateScaOperationCommand command) {
        log.debug("Dispatching initiate-sca saga for resourceId={}", command.getResourceId());

        StepInputs inputs = StepInputs.of(InitiateScaSaga.STEP_CREATE_OPERATION, command);
        return sagaEngine.execute(InitiateScaSaga.SAGA_NAME, inputs);
    }
}
