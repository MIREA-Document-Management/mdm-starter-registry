package ru.mdm.registry.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Настройки отправки информации в реестр.
 */
@Data
@Configuration
@ConfigurationProperties("mdm.registry")
public class RegistryProperties {

    private Duration initialUpdateRegistryInterval = Duration.ofSeconds(30);
    private Duration updateRegistryDuration = Duration.ofSeconds(30);
}
