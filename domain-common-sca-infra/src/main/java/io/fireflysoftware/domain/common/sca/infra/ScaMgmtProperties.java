package io.fireflysoftware.domain.common.sca.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the core-common-sca-mgmt service connection.
 * Mapped from application.yaml under api-configuration.core-platform.sca-mgmt.
 */
@ConfigurationProperties(prefix = "api-configuration.core-platform.sca-mgmt")
@Data
public class ScaMgmtProperties {

    private String basePath;

}
