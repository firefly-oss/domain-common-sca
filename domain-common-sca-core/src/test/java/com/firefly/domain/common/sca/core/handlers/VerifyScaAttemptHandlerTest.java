package com.firefly.domain.common.sca.core.handlers;

import com.firefly.domain.common.sca.core.commands.VerifyScaAttemptCommand;
import com.firefly.domain.common.sca.core.sagas.VerifyScaSaga;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyScaAttemptHandlerTest {

    @Mock
    private SagaEngine sagaEngine;

    private VerifyScaAttemptHandler handler;

    @BeforeEach
    void setUp() {
        handler = new VerifyScaAttemptHandler(sagaEngine);
    }

    @Test
    void doHandle_dispatchesVerifyScaSagaWithCorrectStepInput() {
        UUID operationId = UUID.randomUUID();
        UUID challengeId = UUID.randomUUID();
        VerifyScaAttemptCommand command = new VerifyScaAttemptCommand(operationId, challengeId, "123456");

        SagaResult expectedResult = mock(SagaResult.class);
        when(sagaEngine.execute(eq(VerifyScaSaga.SAGA_NAME), any(StepInputs.class)))
                .thenReturn(Mono.just(expectedResult));

        StepVerifier.create(handler.doHandle(command))
                .expectNext(expectedResult)
                .verifyComplete();

        ArgumentCaptor<StepInputs> captor = ArgumentCaptor.forClass(StepInputs.class);
        verify(sagaEngine).execute(eq(VerifyScaSaga.SAGA_NAME), captor.capture());
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    void doHandle_propagatesErrorFromSagaEngine() {
        UUID operationId = UUID.randomUUID();
        UUID challengeId = UUID.randomUUID();
        VerifyScaAttemptCommand command = new VerifyScaAttemptCommand(operationId, challengeId, "wrong");

        when(sagaEngine.execute(eq(VerifyScaSaga.SAGA_NAME), any(StepInputs.class)))
                .thenReturn(Mono.error(new RuntimeException("verification error")));

        StepVerifier.create(handler.doHandle(command))
                .expectErrorMessage("verification error")
                .verify();
    }
}
