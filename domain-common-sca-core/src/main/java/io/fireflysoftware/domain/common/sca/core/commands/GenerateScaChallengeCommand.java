package io.fireflysoftware.domain.common.sca.core.commands;

import io.fireflysoftware.common.sca.sdk.model.SCAChallengeDTO;
import jakarta.validation.constraints.NotNull;
import org.fireflyframework.cqrs.command.Command;

import java.util.UUID;

/**
 * Command to generate (or regenerate) a challenge for an existing SCA operation.
 */
public class GenerateScaChallengeCommand implements Command<SCAChallengeDTO> {

    @NotNull(message = "operationId is required")
    private final UUID operationId;

    @NotNull(message = "challengeType is required")
    private final ChallengeType challengeType;

    private final String commandId = UUID.randomUUID().toString();

    public GenerateScaChallengeCommand(UUID operationId, ChallengeType challengeType) {
        this.operationId = operationId;
        this.challengeType = challengeType;
    }

    /** {@inheritDoc} */
    @Override
    public String getCommandId() {
        return commandId;
    }

    /**
     * Returns the ID of the SCA operation for which a challenge should be generated.
     *
     * @return the operation identifier
     */
    public UUID getOperationId() {
        return operationId;
    }

    /**
     * Returns the delivery channel type for the generated challenge.
     *
     * @return the challenge type
     */
    public ChallengeType getChallengeType() {
        return challengeType;
    }
}
