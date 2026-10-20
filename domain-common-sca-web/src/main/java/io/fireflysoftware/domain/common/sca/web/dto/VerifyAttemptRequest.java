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
 * Request body for verifying a user's SCA attempt.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to verify a user's SCA attempt")
public class VerifyAttemptRequest {

    @NotNull
    @Schema(description = "Unique identifier of the challenge being answered", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID challengeId;

    @NotBlank
    @Schema(description = "Verification code submitted by the user", example = "123456")
    private String verificationCode;
}
