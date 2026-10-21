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
