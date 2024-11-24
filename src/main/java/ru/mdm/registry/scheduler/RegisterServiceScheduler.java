package ru.mdm.registry.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.mdm.registry.service.StarterRegistryService;

/**
 * Планировщик регистрации сервисов в реестре.
 */
@Component
@RequiredArgsConstructor
public class RegisterServiceScheduler {

    private final StarterRegistryService starterRegistryService;

    @Scheduled(initialDelayString = "#{registryProperties.getInitialUpdateRegistryInterval().toMillis()}",
            fixedRateString = "#{registryProperties.getUpdateRegistryDuration().toMillis()}")
    void registerService() {
        starterRegistryService.registerService();
    }
}
