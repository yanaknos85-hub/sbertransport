package ru.sber.transport.audit.annotation;

import java.lang.annotation.*;

/**
 * Аннотиация для отключения аудита на конкретных эндпоинтах.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Inherited
public @interface NoAudit {
}
