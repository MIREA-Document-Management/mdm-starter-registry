package ru.mdm.registry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mdm.registry.MdmApplicationContext;
import ru.mdm.registry.client.RegistryClient;
import ru.mdm.registry.model.EndpointDto;
import ru.mdm.registry.model.RegisterServiceDto;
import ru.mdm.registry.model.RouteDto;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Сервис стартера реестра.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StarterRegistryService {

    private final MdmApplicationContext mdmContext;
    private final RegistryClient registryClient;

    /**
     * Зарегистрировать сервис в реестре.
     */
    public void registerService() {
        registryClient.registerService(buildRequestForRegistry())
                .doOnSubscribe(subscription -> log.info("Start registration at registry"))
                .doOnError(throwable -> log.error("Error during registration at registry", throwable))
                .doOnSuccess(dto -> log.info("Application was successfully registered at registry"))
                .subscribe();
    }

    public RegisterServiceDto buildRequestForRegistry() {
        try {
            var dto = new RegisterServiceDto();
            dto.setName(mdmContext.getApplicationInfo().getName());
            dto.setDescription(mdmContext.getApplicationInfo().getDescription());
            dto.setHost(InetAddress.getLocalHost().getHostName());

            var route = new RouteDto();
            route.setUri(mdmContext.getApplicationInfo().getRoute().getUri());
            route.setFilters(mdmContext.getApplicationInfo().getRoute().getFilters());
            route.setPredicates(mdmContext.getApplicationInfo().getRoute().getPredicates());
            dto.setRoute(route);

            dto.setVersion(mdmContext.getApplicationInfo().getVersion());
            dto.setEndpoints(
                    mdmContext.getEndpoints().stream()
                            .map(endpoint -> {
                                var endpointDto = new EndpointDto();
                                endpointDto.setPath(endpoint.path());
                                endpointDto.setMethod(endpoint.method());
                                return endpointDto;
                            })
                            .toList()
            );
            return dto;
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }
}
