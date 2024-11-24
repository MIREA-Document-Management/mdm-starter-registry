package ru.mdm.registry.configuration;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Информация о текущем экземпляре приложения.
 */
@Data
@Primary
@Configuration
@ConfigurationProperties(prefix = "mdm.application")
public class ApplicationInfoProperties {

    private String name;
    private String description;
    private String version;
    private Route route;
    private List<String> controllers;

    /**
     * Модель для построения маршрута к текущему экземпляру приложения.
     */
    @Getter
    @RequiredArgsConstructor
    public static class Route {
        private final String uri;
        private final List<String> predicates;
        private final List<String> filters;
    }
}
