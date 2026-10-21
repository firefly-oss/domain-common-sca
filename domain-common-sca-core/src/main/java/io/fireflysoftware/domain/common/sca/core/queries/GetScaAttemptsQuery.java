/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
