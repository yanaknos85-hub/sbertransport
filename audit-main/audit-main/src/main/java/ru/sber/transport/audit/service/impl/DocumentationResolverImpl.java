package ru.sber.transport.audit.service.impl;

import io.swagger.v3.oas.annotations.Operation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.audit.annotation.NoAudit;
import ru.sber.transport.audit.service.DocumentationResolver;

import jakarta.annotation.PostConstruct;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
class DocumentationResolverImpl implements DocumentationResolver {

    private static final String HTTP_DELIMITER = "/";

    private static final String METHOD_URL_FORMAT = "%s %s";

    private Future<Map<String, Description>> foundedMethodsTask = null;

    @PostConstruct
    void init() {
        foundedMethodsTask = Executors.newSingleThreadExecutor().submit(() -> new Reflections("ru", Scanners.MethodsAnnotated)
            .getMethodsAnnotatedWith(Operation.class)
            .stream()
            .map(method -> {
                var operation = method.getAnnotation(Operation.class);
                var audited = !method.isAnnotationPresent(NoAudit.class);
                var description = operation.description();
                var mappingData = getMappingData(method).entrySet();
                if (log.isDebugEnabled()) {
                    log.debug("%s: %s".formatted(mappingData.stream().map(set -> set.getValue().stream().map(HttpMethod::name)
                        .map(mthd -> METHOD_URL_FORMAT.formatted(mthd, set.getKey())).collect(Collectors.joining(", ")))
                        .collect(Collectors.joining(", ")), description));
                }
                return new OperationData(mappingData, new Description(description, audited));
            })
            .distinct()
            .flatMap(entry -> entry.entries().stream().map(e -> new AbstractMap.SimpleEntry<>(e, entry.description())))
            .flatMap(entry -> {
                var methodInfo = entry.getKey();
                return methodInfo.getValue().stream().map(httpMethod -> new AbstractMap.SimpleEntry<>(METHOD_URL_FORMAT.formatted(httpMethod, methodInfo.getKey()), entry.getValue()));
            })
            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (l, r) -> l)));
    }

    @SneakyThrows({ExecutionException.class, InterruptedException.class})
    @Override
    public String getDescription(String method, String url) {
        var founded = foundedMethodsTask.get().get(METHOD_URL_FORMAT.formatted(method, url));
        if (founded!=null){
            return founded.description;
        } else return null;
    }

    @SneakyThrows
    @Override
    public boolean isAuditable(String method, String url) {
        var founded = foundedMethodsTask.get().get(METHOD_URL_FORMAT.formatted(method, url));
        if (founded!=null){
            return founded.audited;
        } else return true;
    }

    private Map<String, Set<HttpMethod>> getMappingData(Method method) {
        var methodClass = method.getDeclaringClass();
        var commonUrls = getCommonUrls(methodClass);
        var result = new HashMap<String, Set<HttpMethod>>();
        for (var annotation : method.getAnnotations()) {
            var methods = HttpMethod.getMethod(annotation);
            var urls = new HashSet<String>();
            for (var m : methods) {
                urls.addAll(getUrls(annotation, m));
            }
            for (var url : urls) {
                var fullPath = new StringBuilder(url);
                for (var commonPath : commonUrls) {
                    var normalized = normalizeUrl(commonPath);
                    fullPath.insert(0, normalized);
                }
                var normalized = normalizeUrl(fullPath.toString());
                result.put(normalized, methods);
            }
        }
        return result;
    }

    private String normalizeUrl(String fullPath) {
        if (!fullPath.startsWith(HTTP_DELIMITER)) {
            fullPath = HTTP_DELIMITER + fullPath;
        }
        if (!fullPath.endsWith(HTTP_DELIMITER)) {
            fullPath = fullPath + HTTP_DELIMITER;
        }
        return fullPath.replace("//", "/");
    }

    private Set<String> getUrls(Annotation annotation, HttpMethod m) {
        return getProxiedData(annotation, m);
    }

    @SneakyThrows(Throwable.class)
    private Set<String> getProxiedData(Annotation annotation, HttpMethod m) {
        InvocationHandler handler;
        if (annotation instanceof Proxy) {
            handler = Proxy.getInvocationHandler(annotation);
        } else {
            handler = (proxy, method, args) -> method.invoke(annotation, args);
        }
        var annotationClass = m.getAnnotation();
        var value = handler.invoke(annotationClass, annotationClass.getDeclaredMethod("value"), null);
        var path = handler.invoke(annotationClass, annotationClass.getDeclaredMethod("path"), null);
        return concatUrl((String[]) value, (String[]) path);
    }

    private HashSet<String> getCommonUrls(Class<?> methodClass) {
        var commonMapping = methodClass.getAnnotation(RequestMapping.class);
        var commonUrls = new HashSet<String>();
        if (commonMapping == null) {
            commonUrls.addAll(Arrays.stream(methodClass.getInterfaces()).parallel().map(i -> i.getAnnotation(RequestMapping.class))
                .filter(Objects::nonNull)
                .map(m -> concatUrl(m.value(), m.path()))
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet()));
        } else {
            commonUrls.addAll(concatUrl(commonMapping.value(), commonMapping.path()));
        }
        return commonUrls;
    }

    private Set<String> concatUrl(String[] value, String[] path) {
        var urls = new HashSet<>(Stream.concat(Arrays.stream(value), Arrays.stream(path)).collect(Collectors.toUnmodifiableSet()));
        if (urls.isEmpty()) {
            urls.add(HTTP_DELIMITER);
        }
        return urls;
    }

    private record OperationData(Set<Map.Entry<String, Set<HttpMethod>>> entries, Description description) {
    }

    private record Description(String description, boolean audited){
    }
}
