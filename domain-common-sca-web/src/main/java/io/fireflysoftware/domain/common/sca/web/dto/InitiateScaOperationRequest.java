package io.fireflysoftware.domain.common.sca.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request body for initiating a new SCA operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to initiate a new SCA operation")
public class InitiateScaOperationRequest {

    @NotBlank
    @Schema(description = "Type of operation requiring SCA (e.g. LOGIN, TRANSFER, PAYMENT)", example = "TRANSFER")
    private String operationType;

    @NotNull
    @Schema(description = "Unique identifier of the resource being protected", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID resourceId;

    @NotBlank
    @Schema(description = "Channel through which the operation is performed (e.g. MOBILE, WEB)", example = "MOBILE")
    private String channel;
}
