package ru.mdm.registry.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Автоконфигурация серверной части реестра
 */
@Configuration
@ComponentScan(basePackages = {"ru.mdm.registry"})
public class MdmRegistryAutoConfiguration {
}
