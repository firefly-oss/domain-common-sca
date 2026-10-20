package io.fireflysoftware.domain.common.sca.infra;

import io.fireflysoftware.common.sca.sdk.api.ScaAttemptControllerApi;
import io.fireflysoftware.common.sca.sdk.api.ScaAuditControllerApi;
import io.fireflysoftware.common.sca.sdk.api.ScaChallengeControllerApi;
import io.fireflysoftware.common.sca.sdk.api.ScaOperationControllerApi;
import io.fireflysoftware.common.sca.sdk.api.ScaOperationHistoryControllerApi;
import io.fireflysoftware.common.sca.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Client factory for the core-common-sca-mgmt SDK.
 * Creates and exposes API beans for SCA operations, challenges, attempts, history, and audit.
 */
@Component
public class ScaMgmtClientFactory {

    private final ApiClient apiClient;

    public ScaMgmtClientFactory(ScaMgmtProperties scaMgmtProperties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(scaMgmtProperties.getBasePath());
    }

    @Bean
    public ScaOperationControllerApi scaOperationControllerApi() {
        return new ScaOperationControllerApi(apiClient);
    }

    @Bean
    public ScaChallengeControllerApi scaChallengeControllerApi() {
        return new ScaChallengeControllerApi(apiClient);
    }

    @Bean
    public ScaAttemptControllerApi scaAttemptControllerApi() {
        return new ScaAttemptControllerApi(apiClient);
    }

    @Bean
    public ScaOperationHistoryControllerApi scaOperationHistoryControllerApi() {
        return new ScaOperationHistoryControllerApi(apiClient);
    }

    @Bean
    public ScaAuditControllerApi scaAuditControllerApi() {
        return new ScaAuditControllerApi(apiClient);
    }

}
