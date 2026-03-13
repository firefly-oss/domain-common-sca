package com.firefly.domain.common.sca.core.handlers;

import com.firefly.common.sca.sdk.api.ScaAttemptControllerApi;
import com.firefly.common.sca.sdk.api.ScaChallengeControllerApi;
import com.firefly.common.sca.sdk.model.PaginationResponseSCAAttemptDTO;
import com.firefly.common.sca.sdk.model.PaginationResponseSCAChallengeDTO;
import com.firefly.common.sca.sdk.model.SCAChallengeDTO;
import com.firefly.domain.common.sca.core.queries.GetScaAttemptsQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetScaAttemptsHandlerTest {

    @Mock
    private ScaChallengeControllerApi scaChallengeApi;

    @Mock
    private ScaAttemptControllerApi scaAttemptApi;

    private GetScaAttemptsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetScaAttemptsHandler(scaChallengeApi, scaAttemptApi);
    }

    @Test
    void doHandle_returnAttemptsForLatestChallenge() {
        UUID operationId = UUID.randomUUID();
        UUID challengeId = UUID.randomUUID();
        GetScaAttemptsQuery query = new GetScaAttemptsQuery(operationId);

        SCAChallengeDTO challenge = new SCAChallengeDTO().scaOperationId(operationId);
        PaginationResponseSCAChallengeDTO challengesPage = new PaginationResponseSCAChallengeDTO()
                .content(List.of(challenge));

        PaginationResponseSCAAttemptDTO attemptsPage = new PaginationResponseSCAAttemptDTO();

        when(scaChallengeApi.getAllChallenges(eq(operationId), eq(0), eq(1), eq("createdAt"), eq("DESC")))
                .thenReturn(Mono.just(challengesPage));
        when(scaAttemptApi.getAllAttempts(eq(operationId), any(), eq(0), eq(10), isNull(), isNull()))
                .thenReturn(Mono.just(attemptsPage));

        StepVerifier.create(handler.doHandle(query))
                .expectNext(attemptsPage)
                .verifyComplete();
    }

    @Test
    void doHandle_returnsEmptyPageWhenNoChallengesExist() {
        UUID operationId = UUID.randomUUID();
        GetScaAttemptsQuery query = new GetScaAttemptsQuery(operationId);

        PaginationResponseSCAChallengeDTO emptyChallenges = new PaginationResponseSCAChallengeDTO()
                .content(List.of());

        when(scaChallengeApi.getAllChallenges(eq(operationId), eq(0), eq(1), eq("createdAt"), eq("DESC")))
                .thenReturn(Mono.just(emptyChallenges));

        StepVerifier.create(handler.doHandle(query))
                .expectNextMatches(page -> page.getContent() == null || page.getContent().isEmpty())
                .verifyComplete();
    }
}
