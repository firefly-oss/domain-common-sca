package com.firefly.domain.common.sca.core.handlers;

import com.firefly.common.sca.sdk.api.ScaOperationHistoryControllerApi;
import com.firefly.common.sca.sdk.model.PaginationResponseSCAOperationHistoryDTO;
import com.firefly.domain.common.sca.core.queries.GetScaStatusHistoryQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetScaStatusHistoryHandlerTest {

    @Mock
    private ScaOperationHistoryControllerApi scaHistoryApi;

    private GetScaStatusHistoryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetScaStatusHistoryHandler(scaHistoryApi);
    }

    @Test
    void doHandle_returnsHistoryPage() {
        UUID operationId = UUID.randomUUID();
        GetScaStatusHistoryQuery query = new GetScaStatusHistoryQuery(operationId);

        PaginationResponseSCAOperationHistoryDTO historyPage = new PaginationResponseSCAOperationHistoryDTO()
                .totalElements(2L);

        when(scaHistoryApi.getAllHistory(operationId, 0, 20, "eventTime", "DESC", any()))
                .thenReturn(Mono.just(historyPage));

        StepVerifier.create(handler.doHandle(query))
                .expectNext(historyPage)
                .verifyComplete();
    }

    @Test
    void doHandle_propagatesErrorFromApi() {
        UUID operationId = UUID.randomUUID();
        GetScaStatusHistoryQuery query = new GetScaStatusHistoryQuery(operationId);

        when(scaHistoryApi.getAllHistory(operationId, 0, 20, "eventTime", "DESC", any()))
                .thenReturn(Mono.error(new RuntimeException("history unavailable")));

        StepVerifier.create(handler.doHandle(query))
                .expectErrorMessage("history unavailable")
                .verify();
    }
}
