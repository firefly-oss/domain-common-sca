package com.firefly.domain.common.sca.core.sagas;

import com.firefly.common.sca.sdk.api.ScaAuditControllerApi;
import com.firefly.common.sca.sdk.api.ScaAttemptControllerApi;
import com.firefly.common.sca.sdk.api.ScaOperationControllerApi;
import com.firefly.common.sca.sdk.model.SCAAuditDTO;
import com.firefly.common.sca.sdk.model.SCAAttemptDTO;
import com.firefly.common.sca.sdk.model.SCAOperationDTO;
import com.firefly.domain.common.sca.core.commands.VerifyScaAttemptCommand;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.orchestration.core.argument.Input;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Orchestrates SCA verification: record attempt → update operation status → write audit trail.
 * On failure, compensates by marking the attempt as failed.
 */
@Slf4j
@Component
@Saga(name = "verify-sca")
public class VerifyScaSaga {

    /** Name of this saga as registered with the SagaEngine. */
    public static final String SAGA_NAME = "verify-sca";

    /** Step ID for the validate-attempt step (also used as the initial StepInputs key). */
    public static final String STEP_VALIDATE_ATTEMPT = "validateAttempt";

    /** Step ID for the update-status step. */
    public static final String STEP_UPDATE_STATUS = "updateStatus";

    /** Step ID for the record-audit step. */
    public static final String STEP_RECORD_AUDIT = "recordAudit";

    static final String CTX_ATTEMPT_ID = "CTX_ATTEMPT_ID";
    static final String CTX_OPERATION_ID = "CTX_OPERATION_ID";
    static final String CTX_CHALLENGE_ID = "CTX_CHALLENGE_ID";

    private final ScaAttemptControllerApi scaAttemptApi;
    private final ScaOperationControllerApi scaOperationApi;
    private final ScaAuditControllerApi scaAuditApi;

    public VerifyScaSaga(ScaAttemptControllerApi scaAttemptApi,
                         ScaOperationControllerApi scaOperationApi,
                         ScaAuditControllerApi scaAuditApi) {
        this.scaAttemptApi = scaAttemptApi;
        this.scaOperationApi = scaOperationApi;
        this.scaAuditApi = scaAuditApi;
    }

    @SagaStep(id = STEP_VALIDATE_ATTEMPT, compensate = "compensateValidateAttempt", retry = 2, backoffMs = 500)
    public Mono<SCAAttemptDTO> validateAttempt(@Input VerifyScaAttemptCommand command, ExecutionContext ctx) {
        log.debug("Recording SCA attempt for operationId={}", command.getOperationId());

        SCAAttemptDTO dto = new SCAAttemptDTO()
                .scaChallengeId(command.getChallengeId())
                .attemptValue(command.getVerificationCode())
                .success(false);

        return scaAttemptApi.createAttempt(
                command.getOperationId(),
                command.getChallengeId(),
                dto
        ).doOnNext(attempt -> {
            ctx.putVariable(CTX_ATTEMPT_ID, attempt);
            ctx.putVariable(CTX_OPERATION_ID, command.getOperationId());
            ctx.putVariable(CTX_CHALLENGE_ID, command.getChallengeId());
        });
    }

    @SagaStep(id = STEP_UPDATE_STATUS, dependsOn = STEP_VALIDATE_ATTEMPT)
    public Mono<SCAOperationDTO> updateStatus(ExecutionContext ctx) {
        SCAAttemptDTO attempt = ctx.getVariableAs(CTX_ATTEMPT_ID, SCAAttemptDTO.class);
        UUID operationId = ctx.getVariableAs(CTX_OPERATION_ID, UUID.class);
        log.debug("Updating SCA operation status for operationId={}", operationId);

        SCAOperationDTO.StatusEnum newStatus = Boolean.TRUE.equals(attempt.getSuccess())
                ? SCAOperationDTO.StatusEnum.VERIFIED
                : SCAOperationDTO.StatusEnum.FAILED;

        SCAOperationDTO dto = new SCAOperationDTO().status(newStatus);

        return scaOperationApi.updateOperation(
                operationId,
                dto
        );
    }

    @SagaStep(id = STEP_RECORD_AUDIT, dependsOn = STEP_UPDATE_STATUS)
    public Mono<SCAAuditDTO> recordAudit(ExecutionContext ctx) {
        SCAAttemptDTO attempt = ctx.getVariableAs(CTX_ATTEMPT_ID, SCAAttemptDTO.class);
        UUID operationId = ctx.getVariableAs(CTX_OPERATION_ID, UUID.class);
        UUID challengeId = ctx.getVariableAs(CTX_CHALLENGE_ID, UUID.class);
        log.debug("Recording audit for operationId={}", operationId);

        SCAAuditDTO dto = new SCAAuditDTO()
                .scaOperationId(operationId)
                .scaChallengeId(challengeId)
                .eventType(Boolean.TRUE.equals(attempt.getSuccess())
                        ? SCAAuditDTO.EventTypeEnum.VERIFIED
                        : SCAAuditDTO.EventTypeEnum.FAILED)
                .details("Verification attempt processed");

        return scaAuditApi.createAuditEvent(
                operationId,
                dto
        );
    }

    /**
     * Compensation: mark the attempt as failed when the saga cannot complete.
     */
    public Mono<Void> compensateValidateAttempt(ExecutionContext ctx) {
        SCAAttemptDTO attempt = ctx.getVariableAs(CTX_ATTEMPT_ID, SCAAttemptDTO.class);
        UUID operationId = ctx.getVariableAs(CTX_OPERATION_ID, UUID.class);
        UUID challengeId = ctx.getVariableAs(CTX_CHALLENGE_ID, UUID.class);
        log.warn("Compensating: marking attempt={} as failed for operationId={}", attempt.getId(), operationId);

        SCAAttemptDTO failedDto = new SCAAttemptDTO()
                .scaChallengeId(challengeId)
                .attemptValue(attempt.getAttemptValue())
                .success(false);

        return scaAttemptApi.updateAttempt(
                operationId,
                challengeId,
                attempt.getId(),
                failedDto
        ).then();
    }
}
