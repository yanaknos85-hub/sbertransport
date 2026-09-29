package ru.sber.transport.audit.annotation;


import java.lang.annotation.*;

/**
 * Аннотиация для аудируемых методов.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Inherited
public @interface Audited {

    /**
     * @return строковое представление действия.
     */
    String source();

    /**
     * @return строковое описание действия. Поддерживается SPeL по аргументам метода.
     */
    String value() default "";

    String format() default "";

    String[] params() default {};
}
