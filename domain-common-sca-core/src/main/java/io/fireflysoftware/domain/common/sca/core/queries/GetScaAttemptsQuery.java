package io.fireflysoftware.domain.common.sca.core.queries;

import io.fireflysoftware.common.sca.sdk.model.PaginationResponseSCAAttemptDTO;
import jakarta.validation.constraints.NotNull;
import org.fireflyframework.cqrs.query.Query;

import java.util.UUID;

/**
 * Query to retrieve all SCA attempts for a given operation.
 * Fetches attempts across all challenges belonging to the operation.
 */
public class GetScaAttemptsQuery implements Query<PaginationResponseSCAAttemptDTO> {

    @NotNull(message = "operationId is required")
    private final UUID operationId;

    public GetScaAttemptsQuery(UUID operationId) {
        this.operationId = operationId;
    }

    /**
     * Returns the identifier of the SCA operation whose attempts should be retrieved.
     *
     * @return the operation identifier
     */
    public UUID getOperationId() {
        return operationId;
    }

    /** {@inheritDoc} */
    @Override
    public String getCacheKey() {
        return "GetScaAttempts:" + operationId;
    }
}
