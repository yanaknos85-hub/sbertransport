package ru.sber.transport.spreadsheet.annotation;

import java.lang.annotation.*;

/**
 * Аннотация для указания, во что отображается NULL-значение в excel-файле либо какое значение считать NULL-ом.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface NullRender {

    /**
     * @return строковое представление отсутствующего значения.
     */
    String value() default "Н/Д";

}
