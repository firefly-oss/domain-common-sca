package io.fireflysoftware.domain.common.sca.core.queries;

import io.fireflysoftware.common.sca.sdk.model.PaginationResponseSCAOperationHistoryDTO;
import jakarta.validation.constraints.NotNull;
import org.fireflyframework.cqrs.query.Query;

import java.util.UUID;

/**
 * Query to retrieve the status-change history for a given SCA operation.
 */
public class GetScaStatusHistoryQuery implements Query<PaginationResponseSCAOperationHistoryDTO> {

    @NotNull(message = "operationId is required")
    private final UUID operationId;

    public GetScaStatusHistoryQuery(UUID operationId) {
        this.operationId = operationId;
    }

    /**
     * Returns the identifier of the SCA operation whose status history should be retrieved.
     *
     * @return the operation identifier
     */
    public UUID getOperationId() {
        return operationId;
    }

    /** {@inheritDoc} */
    @Override
    public String getCacheKey() {
        return "GetScaStatusHistory:" + operationId;
    }
}
