package io.fireflysoftware.domain.common.sca.core.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.fireflyframework.cqrs.command.Command;
import org.fireflyframework.orchestration.saga.engine.SagaResult;

import java.util.UUID;

/**
 * Command to verify a user's SCA attempt against a specific challenge.
 */
public class VerifyScaAttemptCommand implements Command<SagaResult> {

    @NotNull(message = "operationId is required")
    private final UUID operationId;

    @NotNull(message = "challengeId is required")
    private final UUID challengeId;

    @NotBlank(message = "verificationCode is required")
    private final String verificationCode;

    private final String commandId = UUID.randomUUID().toString();

    public VerifyScaAttemptCommand(UUID operationId, UUID challengeId, String verificationCode) {
        this.operationId = operationId;
        this.challengeId = challengeId;
        this.verificationCode = verificationCode;
    }

    /** {@inheritDoc} */
    @Override
    public String getCommandId() {
        return commandId;
    }

    /**
     * Returns the identifier of the SCA operation containing the challenge to verify.
     *
     * @return the operation identifier
     */
    public UUID getOperationId() {
        return operationId;
    }

    /**
     * Returns the identifier of the specific challenge being answered.
     *
     * @return the challenge identifier
     */
    public UUID getChallengeId() {
        return challengeId;
    }

    /**
     * Returns the verification code submitted by the user.
     *
     * @return the verification code
     */
    public String getVerificationCode() {
        return verificationCode;
    }
}
