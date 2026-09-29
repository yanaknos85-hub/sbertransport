package ru.sber.transport.audit.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HTTP-методы доступа.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum HttpMethod {

    /**
     * Получение данных.
     */
    GET(GetMapping.class, RequestMethod.GET),

    /**
     * Обновление данных.
     */
    PUT(PutMapping.class, RequestMethod.PUT),

    /**
     * Создание данных.
     */
    POST(PostMapping.class, RequestMethod.POST),

    /**
     * Удаление данных.
     */
    DELETE(DeleteMapping.class, RequestMethod.DELETE),

    /**
     * Частичное изменение.
     */
    PATCH(PatchMapping.class, RequestMethod.PATCH);

    private final Class<? extends Annotation> annotation;

    private final RequestMethod method;

    /**
     * Получение метода из аннотации.
     *
     * @param annotation аннотация.
     * @return методы, связанные с аннотацией.
     */
    public static Set<HttpMethod> getMethod(Annotation annotation) {
        if (annotation instanceof RequestMapping mapping) {
            return Arrays.stream(mapping.method())
                    .map(HttpMethod::getMethod)
                    .filter(Optional::isPresent)
                    .map(Optional::get).collect(Collectors.toUnmodifiableSet());
        } else {
            return Arrays.stream(HttpMethod.values())
                    .filter(http -> http.getAnnotation().isAssignableFrom(annotation.getClass()))
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    private static Optional<HttpMethod> getMethod(RequestMethod method) {
        return Arrays.stream(HttpMethod.values()).filter(http -> http.getMethod().equals(method)).findFirst();
    }
}
