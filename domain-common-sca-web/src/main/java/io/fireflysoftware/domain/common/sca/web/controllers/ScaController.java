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


package io.fireflysoftware.domain.common.sca.web.controllers;

import io.fireflysoftware.common.sca.sdk.model.PaginationResponseSCAAttemptDTO;
import io.fireflysoftware.common.sca.sdk.model.PaginationResponseSCAOperationHistoryDTO;
import io.fireflysoftware.common.sca.sdk.model.SCAChallengeDTO;
import io.fireflysoftware.common.sca.sdk.model.SCAOperationDTO;
import io.fireflysoftware.domain.common.sca.core.services.ScaService;
import io.fireflysoftware.domain.common.sca.web.dto.GenerateChallengeRequest;
import io.fireflysoftware.domain.common.sca.web.dto.InitiateScaOperationRequest;
import io.fireflysoftware.domain.common.sca.web.dto.VerifyAttemptRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for Strong Customer Authentication (SCA) operations.
 * Exposes endpoints to initiate operations, generate challenges, verify attempts,
 * and query operation state and history.
 */
@Slf4j
@Tag(name = "SCA Operations", description = "APIs for managing Strong Customer Authentication operations")
@RestController
@RequestMapping("/api/v1/sca/operations")
@RequiredArgsConstructor
public class ScaController {

    private final ScaService scaService;

    @Operation(summary = "Initiate SCA Operation",
            description = "Initiates a new SCA operation and generates and delivers its first challenge.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "SCA operation initiated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SagaResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SagaResult>> initiateOperation(
            @Valid @RequestBody InitiateScaOperationRequest request) {
        log.debug("Initiating SCA operation: operationType={}, resourceId={}, channel={}",
                request.getOperationType(), request.getResourceId(), request.getChannel());
        return scaService.initiateOperation(request.getOperationType(), request.getResourceId(), request.getChannel())
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    @Operation(summary = "Get SCA Operation",
            description = "Retrieves an existing SCA operation by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SCA operation retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SCAOperationDTO.class))),
            @ApiResponse(responseCode = "404", description = "SCA operation not found", content = @Content)
    })
    @GetMapping(value = "/{operationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SCAOperationDTO>> getOperation(
            @Parameter(description = "Unique identifier of the SCA operation", required = true)
            @PathVariable UUID operationId) {
        log.debug("Retrieving SCA operation: operationId={}", operationId);
        return scaService.getOperation(operationId)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Generate Challenge",
            description = "Generates or regenerates a challenge for an existing SCA operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Challenge generated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SCAChallengeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "SCA operation not found", content = @Content)
    })
    @PostMapping(value = "/{operationId}/challenge",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SCAChallengeDTO>> generateChallenge(
            @Parameter(description = "Unique identifier of the SCA operation", required = true)
            @PathVariable UUID operationId,
            @Valid @RequestBody GenerateChallengeRequest request) {
        log.debug("Generating challenge: operationId={}, challengeType={}", operationId, request.getChallengeType());
        return scaService.generateChallenge(operationId, request.getChallengeType())
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    @Operation(summary = "Verify SCA Attempt",
            description = "Verifies a user's SCA attempt, updates the operation status, and records an audit event.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempt verified",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SagaResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "SCA operation not found", content = @Content)
    })
    @PostMapping(value = "/{operationId}/verify",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SagaResult>> verifyAttempt(
            @Parameter(description = "Unique identifier of the SCA operation", required = true)
            @PathVariable UUID operationId,
            @Valid @RequestBody VerifyAttemptRequest request) {
        log.debug("Verifying SCA attempt: operationId={}, challengeId={}", operationId, request.getChallengeId());
        return scaService.verifyAttempt(operationId, request.getChallengeId(), request.getVerificationCode())
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Get SCA Attempts",
            description = "Retrieves all attempts for the latest challenge on the given SCA operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempts retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PaginationResponseSCAAttemptDTO.class))),
            @ApiResponse(responseCode = "404", description = "SCA operation not found", content = @Content)
    })
    @GetMapping(value = "/{operationId}/attempts", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PaginationResponseSCAAttemptDTO>> getAttempts(
            @Parameter(description = "Unique identifier of the SCA operation", required = true)
            @PathVariable UUID operationId) {
        log.debug("Retrieving SCA attempts: operationId={}", operationId);
        return scaService.getAttempts(operationId)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Get Status History",
            description = "Retrieves the status-change history for the given SCA operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status history retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PaginationResponseSCAOperationHistoryDTO.class))),
            @ApiResponse(responseCode = "404", description = "SCA operation not found", content = @Content)
    })
    @GetMapping(value = "/{operationId}/status-history", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PaginationResponseSCAOperationHistoryDTO>> getStatusHistory(
            @Parameter(description = "Unique identifier of the SCA operation", required = true)
            @PathVariable UUID operationId) {
        log.debug("Retrieving SCA status history: operationId={}", operationId);
        return scaService.getStatusHistory(operationId)
                .map(ResponseEntity::ok);
    }
}
