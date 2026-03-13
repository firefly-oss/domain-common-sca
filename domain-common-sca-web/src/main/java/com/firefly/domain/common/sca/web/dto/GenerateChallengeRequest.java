package com.firefly.domain.common.sca.web.dto;

import com.firefly.domain.common.sca.core.commands.ChallengeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for generating or regenerating a challenge for an existing SCA operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate a challenge for an existing SCA operation")
public class GenerateChallengeRequest {

    @NotNull
    @Schema(description = "Type of challenge to generate", example = "OTP")
    private ChallengeType challengeType;
}
