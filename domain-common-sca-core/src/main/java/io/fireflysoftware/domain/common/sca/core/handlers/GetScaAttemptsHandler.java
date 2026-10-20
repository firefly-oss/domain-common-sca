package io.fireflysoftware.domain.common.sca.core.handlers;

import io.fireflysoftware.common.sca.sdk.api.ScaAttemptControllerApi;
import io.fireflysoftware.common.sca.sdk.api.ScaChallengeControllerApi;
import io.fireflysoftware.common.sca.sdk.model.PaginationResponseSCAAttemptDTO;
import io.fireflysoftware.common.sca.sdk.model.SCAChallengeDTO;
import io.fireflysoftware.domain.common.sca.core.queries.GetScaAttemptsQuery;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.QueryHandlerComponent;
import org.fireflyframework.cqrs.query.QueryHandler;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Handles {@link GetScaAttemptsQuery} by resolving the latest challenge for the operation
 * and returning its attempts page.
 */
@Slf4j
@QueryHandlerComponent(cacheable = false, metrics = true)
public class GetScaAttemptsHandler extends QueryHandler<GetScaAttemptsQuery, PaginationResponseSCAAttemptDTO> {

    private final ScaChallengeControllerApi scaChallengeApi;
    private final ScaAttemptControllerApi scaAttemptApi;

    public GetScaAttemptsHandler(ScaChallengeControllerApi scaChallengeApi,
                                  ScaAttemptControllerApi scaAttemptApi) {
        this.scaChallengeApi = scaChallengeApi;
        this.scaAttemptApi = scaAttemptApi;
    }

    @Override
    protected Mono<PaginationResponseSCAAttemptDTO> doHandle(GetScaAttemptsQuery query) {
        log.debug("Fetching SCA attempts for operationId={}", query.getOperationId());

        return scaChallengeApi.getAllChallenges(query.getOperationId(), 0, 1, "createdAt", "DESC", UUID.randomUUID().toString())
                .flatMap(challenges -> {
                    if (challenges == null
                            || challenges.getContent() == null
                            || challenges.getContent().isEmpty()) {
                        return Mono.just(new PaginationResponseSCAAttemptDTO());
                    }
                    SCAChallengeDTO latest = challenges.getContent().get(0);
                    return scaAttemptApi.getAllAttempts(
                            query.getOperationId(), latest.getId(), 0, 10, null, null, null
                    );
                });
    }
}
