package ru.mdm.registry;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.mdm.registry.annotation.MdmEndpointController;
import ru.mdm.registry.configuration.ApplicationInfoProperties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Вспомогательный контекст, содержащий информации о текущем экземпляре приложения.
 */
@Slf4j
@Getter
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties({ApplicationInfoProperties.class})
public class MdmApplicationContext {

    private final ApplicationInfoProperties applicationInfo;

    private final Set<Endpoint> endpoints = new HashSet<>();

    @PostConstruct
    public void initContext() {
        log.info("Start filling MdmApplicationContext");
        logApplicationInfoProperties();
        fillEndpoints();
        log.info("Successfully finish filling context");
    }

    private void fillEndpoints() {
        log.debug("Start filling endpoints");
        var provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(MdmEndpointController.class));
        if (applicationInfo.getControllers() != null) {
            applicationInfo.getControllers().stream()
                    .map(provider::findCandidateComponents)
                    .flatMap(Collection::stream)
                    .map(BeanDefinition::getBeanClassName)
                    .forEach(beanClassName -> {
                        try {
                            Class<?> dataTypeClass = Class.forName(beanClassName);
                            addEndpointsFromController(dataTypeClass);
                        } catch (ClassNotFoundException e) {
                            log.error("Error while filling endpoints", e);
                        }
                    });
        }
    }

    private void addEndpointsFromController(Class<?> beanClass) {
        Class<?>[] interfaces = beanClass.getInterfaces();
        RequestMapping requestMappingAnnotation;
        if (beanClass.isAnnotationPresent(RequestMapping.class)) {
            requestMappingAnnotation = beanClass.getAnnotation(RequestMapping.class);
        } else {
            requestMappingAnnotation = null;
        }
        addEndpointsFromAllMethods(requestMappingAnnotation, beanClass);
        Arrays.stream(interfaces)
                .filter(interfacee -> interfacee.isAnnotationPresent(MdmEndpointController.class))
                .forEach(interfacee -> addEndpointsFromAllMethods(requestMappingAnnotation, interfacee));
        log.info("Endpoints were added into context: {}",
                endpoints.stream().map(Endpoint::path).reduce((a, b) -> a + ", " + b).orElse(""));
    }

    private void addEndpointsFromAllMethods(RequestMapping requestMappingAnnotation, Class<?> clazz) {
        Arrays.stream(clazz.getMethods())
                .forEach(method -> addEndpointsWithRequestMapping(requestMappingAnnotation, method));
    }

    private void addEndpointsWithRequestMapping(RequestMapping ofClass, Method method) {
        Annotation[] requestMappingParent = new Annotation[1];
        Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(RequestMapping.class))
                .map(annotation -> {
                    requestMappingParent[0] = annotation;
                    return annotation.annotationType().getAnnotation(RequestMapping.class);
                })
                .findFirst()
                .ifPresent(requestMappingOfMethod ->
                        endpoints.add(buildEndpoint(ofClass, requestMappingOfMethod, requestMappingParent[0])));
    }

    private Endpoint buildEndpoint(RequestMapping ofClass, RequestMapping ofMethod, Annotation ofMethodParentAnnotation) {
        String[] classPathArr;
        if (ofClass != null) {
            classPathArr = StringUtils.isAllBlank(ofClass.value()) ? ofClass.path() : ofClass.value();
        } else {
            classPathArr = new String[0];
        }

        String[] methodPathArr;
        methodPathArr = getMethodPathArr(ofMethodParentAnnotation);
        String classPath = classPathArr.length > 0 ? classPathArr[0] : "";
        String methodPath = methodPathArr.length > 0 ? methodPathArr[0] : "";
        return new Endpoint(classPath + methodPath, ofMethod.method()[0]);
    }

    private String[] getMethodPathArr(Annotation ofMethodParentAnnotation) {
        String[] methodPathArr;
        switch (ofMethodParentAnnotation.annotationType().getSimpleName()) {
            case "PutMapping": {
                PutMapping ofMethodParentAnnotationCasted = (PutMapping) ofMethodParentAnnotation;
                methodPathArr = StringUtils.isAllBlank(ofMethodParentAnnotationCasted.value())
                        ? ofMethodParentAnnotationCasted.path() : ofMethodParentAnnotationCasted.value();
                break;
            }
            case "PostMapping": {
                PostMapping ofMethodParentAnnotationCasted = (PostMapping) ofMethodParentAnnotation;
                methodPathArr = StringUtils.isAllBlank(ofMethodParentAnnotationCasted.value())
                        ? ofMethodParentAnnotationCasted.path() : ofMethodParentAnnotationCasted.value();
                break;
            }
            case "PatchMapping": {
                PatchMapping ofMethodParentAnnotationCasted = (PatchMapping) ofMethodParentAnnotation;
                methodPathArr = StringUtils.isAllBlank(ofMethodParentAnnotationCasted.value())
                        ? ofMethodParentAnnotationCasted.path() : ofMethodParentAnnotationCasted.value();
                break;
            }
            case "DeleteMapping": {
                DeleteMapping ofMethodParentAnnotationCasted = (DeleteMapping) ofMethodParentAnnotation;
                methodPathArr = StringUtils.isAllBlank(ofMethodParentAnnotationCasted.value())
                        ? ofMethodParentAnnotationCasted.path() : ofMethodParentAnnotationCasted.value();
                break;
            }
            case "GetMapping": {
                GetMapping ofMethodParentAnnotationCasted = (GetMapping) ofMethodParentAnnotation;
                methodPathArr = StringUtils.isAllBlank(ofMethodParentAnnotationCasted.value())
                        ? ofMethodParentAnnotationCasted.path() : ofMethodParentAnnotationCasted.value();
                break;
            }
            default:
                throw new IllegalStateException("Недопустимый метод эндпоинта: " +
                        ofMethodParentAnnotation.annotationType().getSimpleName());
        }
        return methodPathArr;
    }

    private void logApplicationInfoProperties() {
        log.info("Application name: {}", applicationInfo.getName());
        log.info("Application description: {}", applicationInfo.getDescription());
        log.info("Application version: {}", applicationInfo.getVersion());
        log.info("Application uri: {}", applicationInfo.getRoute().getUri());
    }

    public record Endpoint(String path, RequestMethod method) { }
}
