package io.fireflysoftware.domain.common.sca.core.handlers;

import io.fireflysoftware.common.sca.sdk.api.ScaOperationHistoryControllerApi;
import io.fireflysoftware.common.sca.sdk.model.PaginationResponseSCAOperationHistoryDTO;
import io.fireflysoftware.domain.common.sca.core.queries.GetScaStatusHistoryQuery;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.QueryHandlerComponent;
import org.fireflyframework.cqrs.query.QueryHandler;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Handles {@link GetScaStatusHistoryQuery} by fetching the operation status-change history.
 */
@Slf4j
@QueryHandlerComponent(cacheable = true, cacheTtl = 60, metrics = true)
public class GetScaStatusHistoryHandler extends QueryHandler<GetScaStatusHistoryQuery, PaginationResponseSCAOperationHistoryDTO> {

    private final ScaOperationHistoryControllerApi scaHistoryApi;

    public GetScaStatusHistoryHandler(ScaOperationHistoryControllerApi scaHistoryApi) {
        this.scaHistoryApi = scaHistoryApi;
    }

    @Override
    protected Mono<PaginationResponseSCAOperationHistoryDTO> doHandle(GetScaStatusHistoryQuery query) {
        log.debug("Fetching status history for operationId={}", query.getOperationId());

        return scaHistoryApi.getAllHistory(query.getOperationId(), 0, 20, "eventTime", "DESC", UUID.randomUUID().toString());
    }
}
