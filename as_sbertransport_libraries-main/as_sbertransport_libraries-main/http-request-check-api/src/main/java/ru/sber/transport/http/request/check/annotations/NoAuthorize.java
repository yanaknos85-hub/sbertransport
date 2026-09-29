package ru.sber.transport.http.request.check.annotations;

import java.lang.annotation.*;

/**
 * Аннотация для снятия с авторизацией.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented
@Inherited
public @interface NoAuthorize {

    /**
     * @return адрес для снятия с авторизацией. Перечислять через запятую.
     */
    String value() default "";

}
