package com.firefly.domain.common.sca.core.handlers;

import com.firefly.common.sca.sdk.api.ScaOperationControllerApi;
import com.firefly.common.sca.sdk.model.SCAOperationDTO;
import com.firefly.domain.common.sca.core.queries.GetScaOperationQuery;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.QueryHandlerComponent;
import org.fireflyframework.cqrs.query.QueryHandler;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Handles {@link GetScaOperationQuery} by fetching the operation from the SCA management API.
 */
@Slf4j
@QueryHandlerComponent(cacheable = true, cacheTtl = 30, metrics = true)
public class GetScaOperationHandler extends QueryHandler<GetScaOperationQuery, SCAOperationDTO> {

    private final ScaOperationControllerApi scaOperationApi;

    public GetScaOperationHandler(ScaOperationControllerApi scaOperationApi) {
        this.scaOperationApi = scaOperationApi;
    }

    @Override
    protected Mono<SCAOperationDTO> doHandle(GetScaOperationQuery query) {
        log.debug("Fetching SCA operation operationId={}", query.getOperationId());

        return scaOperationApi.getOperation(query.getOperationId(), UUID.randomUUID().toString());
    }
}
