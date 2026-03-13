package com.firefly.domain.common.sca.web.controllers;

import com.firefly.common.sca.sdk.model.PaginationResponseSCAAttemptDTO;
import com.firefly.common.sca.sdk.model.PaginationResponseSCAOperationHistoryDTO;
import com.firefly.common.sca.sdk.model.SCAChallengeDTO;
import com.firefly.common.sca.sdk.model.SCAOperationDTO;
import com.firefly.domain.common.sca.core.commands.ChallengeType;
import com.firefly.domain.common.sca.core.services.ScaService;
import com.firefly.domain.common.sca.web.dto.GenerateChallengeRequest;
import com.firefly.domain.common.sca.web.dto.InitiateScaOperationRequest;
import com.firefly.domain.common.sca.web.dto.VerifyAttemptRequest;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScaControllerTest {

    @Mock
    private ScaService scaService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(new ScaController(scaService)).build();
    }

    // ── initiateOperation ────────────────────────────────────────────────────

    @Test
    void initiateOperation_validRequest_returns201() {
        SagaResult result = mock(SagaResult.class);
        when(scaService.initiateOperation(anyString(), any(UUID.class), anyString()))
                .thenReturn(Mono.just(result));

        InitiateScaOperationRequest request = InitiateScaOperationRequest.builder()
                .operationType("TRANSFER")
                .resourceId(UUID.randomUUID())
                .channel("MOBILE")
                .build();

        webTestClient.post().uri("/api/v1/sca/operations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        verify(scaService).initiateOperation(eq("TRANSFER"), any(UUID.class), eq("MOBILE"));
    }

    @Test
    void initiateOperation_whenServiceFails_propagatesError() {
        when(scaService.initiateOperation(anyString(), any(UUID.class), anyString()))
                .thenReturn(Mono.error(new RuntimeException("downstream failure")));

        InitiateScaOperationRequest request = InitiateScaOperationRequest.builder()
                .operationType("LOGIN")
                .resourceId(UUID.randomUUID())
                .channel("WEB")
                .build();

        // Error propagation relies on the global error handler (ControllerAdvice / WebFlux defaults)
        webTestClient.post().uri("/api/v1/sca/operations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ── getOperation ─────────────────────────────────────────────────────────

    @Test
    void getOperation_existingOperationId_returns200() {
        UUID operationId = UUID.randomUUID();
        SCAOperationDTO dto = mock(SCAOperationDTO.class);
        when(scaService.getOperation(operationId)).thenReturn(Mono.just(dto));

        webTestClient.get().uri("/api/v1/sca/operations/{id}", operationId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        verify(scaService).getOperation(operationId);
    }

    @Test
    void getOperation_whenServiceFails_propagatesError() {
        UUID operationId = UUID.randomUUID();
        when(scaService.getOperation(operationId))
                .thenReturn(Mono.error(new RuntimeException("not found")));

        webTestClient.get().uri("/api/v1/sca/operations/{id}", operationId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ── generateChallenge ────────────────────────────────────────────────────

    @Test
    void generateChallenge_validRequest_returns201() {
        UUID operationId = UUID.randomUUID();
        SCAChallengeDTO dto = mock(SCAChallengeDTO.class);
        when(scaService.generateChallenge(eq(operationId), eq(ChallengeType.OTP)))
                .thenReturn(Mono.just(dto));

        GenerateChallengeRequest request = GenerateChallengeRequest.builder()
                .challengeType(ChallengeType.OTP)
                .build();

        webTestClient.post().uri("/api/v1/sca/operations/{id}/challenge", operationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        verify(scaService).generateChallenge(operationId, ChallengeType.OTP);
    }

    @Test
    void generateChallenge_whenServiceFails_propagatesError() {
        UUID operationId = UUID.randomUUID();
        when(scaService.generateChallenge(any(UUID.class), any(ChallengeType.class)))
                .thenReturn(Mono.error(new RuntimeException("challenge generation failed")));

        GenerateChallengeRequest request = GenerateChallengeRequest.builder()
                .challengeType(ChallengeType.PUSH)
                .build();

        webTestClient.post().uri("/api/v1/sca/operations/{id}/challenge", operationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ── verifyAttempt ────────────────────────────────────────────────────────

    @Test
    void verifyAttempt_validRequest_returns200() {
        UUID operationId = UUID.randomUUID();
        UUID challengeId = UUID.randomUUID();
        SagaResult result = mock(SagaResult.class);
        when(scaService.verifyAttempt(eq(operationId), eq(challengeId), anyString()))
                .thenReturn(Mono.just(result));

        VerifyAttemptRequest request = VerifyAttemptRequest.builder()
                .challengeId(challengeId)
                .verificationCode("123456")
                .build();

        webTestClient.post().uri("/api/v1/sca/operations/{id}/verify", operationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        verify(scaService).verifyAttempt(operationId, challengeId, "123456");
    }

    @Test
    void verifyAttempt_whenServiceFails_propagatesError() {
        UUID operationId = UUID.randomUUID();
        when(scaService.verifyAttempt(any(UUID.class), any(UUID.class), anyString()))
                .thenReturn(Mono.error(new RuntimeException("verification failed")));

        VerifyAttemptRequest request = VerifyAttemptRequest.builder()
                .challengeId(UUID.randomUUID())
                .verificationCode("000000")
                .build();

        webTestClient.post().uri("/api/v1/sca/operations/{id}/verify", operationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ── getAttempts ──────────────────────────────────────────────────────────

    @Test
    void getAttempts_existingOperationId_returns200() {
        UUID operationId = UUID.randomUUID();
        PaginationResponseSCAAttemptDTO dto = mock(PaginationResponseSCAAttemptDTO.class);
        when(scaService.getAttempts(operationId)).thenReturn(Mono.just(dto));

        webTestClient.get().uri("/api/v1/sca/operations/{id}/attempts", operationId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        verify(scaService).getAttempts(operationId);
    }

    @Test
    void getAttempts_whenServiceFails_propagatesError() {
        UUID operationId = UUID.randomUUID();
        when(scaService.getAttempts(operationId))
                .thenReturn(Mono.error(new RuntimeException("attempts fetch failed")));

        webTestClient.get().uri("/api/v1/sca/operations/{id}/attempts", operationId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ── getStatusHistory ─────────────────────────────────────────────────────

    @Test
    void getStatusHistory_existingOperationId_returns200() {
        UUID operationId = UUID.randomUUID();
        PaginationResponseSCAOperationHistoryDTO dto = mock(PaginationResponseSCAOperationHistoryDTO.class);
        when(scaService.getStatusHistory(operationId)).thenReturn(Mono.just(dto));

        webTestClient.get().uri("/api/v1/sca/operations/{id}/status-history", operationId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        verify(scaService).getStatusHistory(operationId);
    }

    @Test
    void getStatusHistory_whenServiceFails_propagatesError() {
        UUID operationId = UUID.randomUUID();
        when(scaService.getStatusHistory(operationId))
                .thenReturn(Mono.error(new RuntimeException("history fetch failed")));

        webTestClient.get().uri("/api/v1/sca/operations/{id}/status-history", operationId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
