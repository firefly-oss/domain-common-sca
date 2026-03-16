package com.firefly.domain.common.sca.core.handlers;

import com.firefly.common.sca.sdk.api.ScaOperationControllerApi;
import com.firefly.common.sca.sdk.model.SCAOperationDTO;
import com.firefly.domain.common.sca.core.queries.GetScaOperationQuery;
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
class GetScaOperationHandlerTest {

    @Mock
    private ScaOperationControllerApi scaOperationApi;

    private GetScaOperationHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetScaOperationHandler(scaOperationApi);
    }

    @Test
    void doHandle_returnsOperationFromApi() {
        UUID operationId = UUID.randomUUID();
        GetScaOperationQuery query = new GetScaOperationQuery(operationId);

        SCAOperationDTO dto = new SCAOperationDTO().status(SCAOperationDTO.StatusEnum.PENDING);
        when(scaOperationApi.getOperation(operationId, any()))
                .thenReturn(Mono.just(dto));

        StepVerifier.create(handler.doHandle(query))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void doHandle_propagatesErrorFromApi() {
        UUID operationId = UUID.randomUUID();
        GetScaOperationQuery query = new GetScaOperationQuery(operationId);

        when(scaOperationApi.getOperation(operationId, any()))
                .thenReturn(Mono.error(new RuntimeException("not found")));

        StepVerifier.create(handler.doHandle(query))
                .expectErrorMessage("not found")
                .verify();
    }
}
