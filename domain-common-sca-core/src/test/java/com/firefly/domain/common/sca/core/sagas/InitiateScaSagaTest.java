package com.firefly.domain.common.sca.core.sagas;

import com.firefly.common.sca.sdk.api.ScaAttemptControllerApi;
import com.firefly.common.sca.sdk.api.ScaAuditControllerApi;
import com.firefly.common.sca.sdk.api.ScaChallengeControllerApi;
import com.firefly.common.sca.sdk.api.ScaOperationControllerApi;
import com.firefly.common.sca.sdk.api.ScaOperationHistoryControllerApi;
import com.firefly.common.sca.sdk.model.SCAChallengeDTO;
import com.firefly.common.sca.sdk.model.SCAOperationDTO;
import com.firefly.domain.common.sca.core.TestScaCoreApplication;
import com.firefly.domain.common.sca.core.commands.InitiateScaOperationCommand;
import com.firefly.domain.common.sca.core.sagas.InitiateScaSaga;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestScaCoreApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class InitiateScaSagaTest {

    @Autowired
    private SagaEngine sagaEngine;

    @MockBean
    private ScaOperationControllerApi scaOperationApi;

    @MockBean
    private ScaChallengeControllerApi scaChallengeApi;

    // Required by other handlers in the same Spring context
    @MockBean
    private ScaAttemptControllerApi scaAttemptApi;

    @MockBean
    private ScaAuditControllerApi scaAuditApi;

    @MockBean
    private ScaOperationHistoryControllerApi scaHistoryApi;

    @Test
    void happyPath_createsOperationAndChallengeAndTriggers() {
        UUID operationId = UUID.randomUUID();
        UUID challengeId = UUID.randomUUID();

        SCAOperationDTO createdOperation = new SCAOperationDTO()
                .status(SCAOperationDTO.StatusEnum.PENDING);

        SCAChallengeDTO createdChallenge = new SCAChallengeDTO()
                .scaOperationId(operationId);

        when(scaOperationApi.createOperation(any(SCAOperationDTO.class)))
                .thenReturn(Mono.just(createdOperation));
        when(scaChallengeApi.createChallenge(any(), any(SCAChallengeDTO.class)))
                .thenReturn(Mono.just(createdChallenge));
        when(scaOperationApi.triggerSCA(any()))
                .thenReturn(Mono.empty());

        InitiateScaOperationCommand command = new InitiateScaOperationCommand("LOGIN", UUID.randomUUID(), "MOBILE");
        StepInputs inputs = StepInputs.of(InitiateScaSaga.STEP_CREATE_OPERATION, command);

        StepVerifier.create(sagaEngine.execute(InitiateScaSaga.SAGA_NAME, inputs))
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isTrue();
                    assertThat(result.steps()).containsKeys(InitiateScaSaga.STEP_CREATE_OPERATION, InitiateScaSaga.STEP_GENERATE_CHALLENGE, InitiateScaSaga.STEP_SEND_CHALLENGE);
                })
                .verifyComplete();
    }

    @Test
    void compensationTriggered_whenGenerateChallengeStepFails() {
        UUID operationId = UUID.randomUUID();

        SCAOperationDTO createdOperation = new SCAOperationDTO()
                .status(SCAOperationDTO.StatusEnum.PENDING);

        when(scaOperationApi.createOperation(any(SCAOperationDTO.class)))
                .thenReturn(Mono.just(createdOperation));
        when(scaChallengeApi.createChallenge(any(), any(SCAChallengeDTO.class)))
                .thenReturn(Mono.error(new RuntimeException("challenge service unavailable")));
        when(scaOperationApi.updateOperation(any(), any(SCAOperationDTO.class)))
                .thenReturn(Mono.just(new SCAOperationDTO().status(SCAOperationDTO.StatusEnum.CANCELLED)));

        InitiateScaOperationCommand command = new InitiateScaOperationCommand("TRANSFER", UUID.randomUUID(), "WEB");
        StepInputs inputs = StepInputs.of(InitiateScaSaga.STEP_CREATE_OPERATION, command);

        StepVerifier.create(sagaEngine.execute(InitiateScaSaga.SAGA_NAME, inputs))
                .assertNext(result -> {
                    assertThat(result.isFailed()).isTrue();
                    assertThat(result.compensatedSteps()).contains(InitiateScaSaga.STEP_CREATE_OPERATION);
                })
                .verifyComplete();
    }
}
