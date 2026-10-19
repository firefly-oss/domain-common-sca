package com.firefly.domain.common.sca.core.sagas;

import com.firefly.common.sca.sdk.api.ScaAuditControllerApi;
import com.firefly.common.sca.sdk.api.ScaAttemptControllerApi;
import com.firefly.common.sca.sdk.api.ScaChallengeControllerApi;
import com.firefly.common.sca.sdk.api.ScaOperationControllerApi;
import com.firefly.common.sca.sdk.api.ScaOperationHistoryControllerApi;
import com.firefly.common.sca.sdk.model.SCAAuditDTO;
import com.firefly.common.sca.sdk.model.SCAAttemptDTO;
import com.firefly.common.sca.sdk.model.SCAOperationDTO;
import com.firefly.domain.common.sca.core.TestScaCoreApplication;
import com.firefly.domain.common.sca.core.commands.VerifyScaAttemptCommand;
import com.firefly.domain.common.sca.core.sagas.VerifyScaSaga;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestScaCoreApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class VerifyScaSagaTest {

    @Autowired
    private SagaEngine sagaEngine;

    @MockitoBean
    private ScaAttemptControllerApi scaAttemptApi;

    @MockitoBean
    private ScaOperationControllerApi scaOperationApi;

    @MockitoBean
    private ScaAuditControllerApi scaAuditApi;

    @MockitoBean
    private ScaChallengeControllerApi scaChallengeApi;

    @MockitoBean
    private ScaOperationHistoryControllerApi scaOperationHistoryApi;

    @Test
    void happyPath_recordsAttemptUpdatesStatusAndAudit() {
        UUID operationId = UUID.randomUUID();
        UUID challengeId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();

        SCAAttemptDTO attempt = new SCAAttemptDTO()
                .scaChallengeId(challengeId)
                .attemptValue("123456")
                .success(true);

        SCAOperationDTO updatedOp = new SCAOperationDTO().status(SCAOperationDTO.StatusEnum.VERIFIED);
        SCAAuditDTO audit = new SCAAuditDTO().eventType(SCAAuditDTO.EventTypeEnum.VERIFIED);

        when(scaAttemptApi.createAttempt(any(), any(), any(SCAAttemptDTO.class), any()))
                .thenReturn(Mono.just(attempt));
        when(scaOperationApi.updateOperation(any(), any(SCAOperationDTO.class), any()))
                .thenReturn(Mono.just(updatedOp));
        when(scaAuditApi.createAuditEvent(any(), any(SCAAuditDTO.class), any()))
                .thenReturn(Mono.just(audit));

        VerifyScaAttemptCommand command = new VerifyScaAttemptCommand(operationId, challengeId, "123456");
        StepInputs inputs = StepInputs.of(VerifyScaSaga.STEP_VALIDATE_ATTEMPT, command);

        StepVerifier.create(sagaEngine.execute(VerifyScaSaga.SAGA_NAME, inputs))
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isTrue();
                    assertThat(result.steps()).containsKeys(VerifyScaSaga.STEP_VALIDATE_ATTEMPT, VerifyScaSaga.STEP_UPDATE_STATUS, VerifyScaSaga.STEP_RECORD_AUDIT);
                })
                .verifyComplete();
    }

    @Test
    void compensationTriggered_whenUpdateStatusStepFails() {
        UUID operationId = UUID.randomUUID();
        UUID challengeId = UUID.randomUUID();

        SCAAttemptDTO attempt = new SCAAttemptDTO()
                .scaChallengeId(challengeId)
                .attemptValue("000000")
                .success(false);

        when(scaAttemptApi.createAttempt(any(), any(), any(SCAAttemptDTO.class), any()))
                .thenReturn(Mono.just(attempt));
        when(scaOperationApi.updateOperation(any(), any(SCAOperationDTO.class), any()))
                .thenReturn(Mono.error(new RuntimeException("status update failed")));
        when(scaAttemptApi.updateAttempt(any(), any(), any(), any(SCAAttemptDTO.class), any()))
                .thenReturn(Mono.just(new SCAAttemptDTO().success(false)));

        VerifyScaAttemptCommand command = new VerifyScaAttemptCommand(operationId, challengeId, "000000");
        StepInputs inputs = StepInputs.of(VerifyScaSaga.STEP_VALIDATE_ATTEMPT, command);

        StepVerifier.create(sagaEngine.execute(VerifyScaSaga.SAGA_NAME, inputs))
                .assertNext(result -> {
                    assertThat(result.isFailed()).isTrue();
                    assertThat(result.compensatedSteps()).contains(VerifyScaSaga.STEP_VALIDATE_ATTEMPT);
                })
                .verifyComplete();
    }
}
