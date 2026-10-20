package io.fireflysoftware.domain.common.sca.core.queries;

import io.fireflysoftware.common.sca.sdk.model.SCAOperationDTO;
import jakarta.validation.constraints.NotNull;
import org.fireflyframework.cqrs.query.Query;

import java.util.UUID;

/**
 * Query to retrieve a single SCA operation by its identifier.
 */
public class GetScaOperationQuery implements Query<SCAOperationDTO> {

    @NotNull(message = "operationId is required")
    private final UUID operationId;

    public GetScaOperationQuery(UUID operationId) {
        this.operationId = operationId;
    }

    /**
     * Returns the identifier of the SCA operation to retrieve.
     *
     * @return the operation identifier
     */
    public UUID getOperationId() {
        return operationId;
    }

    /** {@inheritDoc} */
    @Override
    public String getCacheKey() {
        return "GetScaOperation:" + operationId;
    }
}
