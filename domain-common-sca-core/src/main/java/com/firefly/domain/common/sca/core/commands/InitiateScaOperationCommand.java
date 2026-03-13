package com.firefly.domain.common.sca.core.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.fireflyframework.cqrs.command.Command;
import org.fireflyframework.orchestration.saga.engine.SagaResult;

import java.util.UUID;

/**
 * Command to initiate a new SCA operation for a given resource and channel.
 */
public class InitiateScaOperationCommand implements Command<SagaResult> {

    @NotBlank(message = "operationType is required")
    private final String operationType;

    @NotNull(message = "resourceId is required")
    private final UUID resourceId;

    @NotBlank(message = "channel is required")
    private final String channel;

    private final String commandId = UUID.randomUUID().toString();

    public InitiateScaOperationCommand(String operationType, UUID resourceId, String channel) {
        this.operationType = operationType;
        this.resourceId = resourceId;
        this.channel = channel;
    }

    /** {@inheritDoc} */
    @Override
    public String getCommandId() {
        return commandId;
    }

    /**
     * Returns the operation type (e.g. {@code LOGIN}, {@code TRANSFER}) for the SCA operation.
     *
     * @return the operation type string
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * Returns the unique identifier of the resource being protected by SCA.
     *
     * @return the resource identifier
     */
    public UUID getResourceId() {
        return resourceId;
    }

    /**
     * Returns the channel through which the operation is performed (e.g. {@code MOBILE}, {@code WEB}).
     *
     * @return the channel string
     */
    public String getChannel() {
        return channel;
    }
}
