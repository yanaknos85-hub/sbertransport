package ru.sber.transport.spreadsheet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Формат для полей времени.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TimeFormat {

    /**
     * @return формат.
     */
    Format value();

    /**
     * @return формат времени и даты при выборе {@link TimeFormat#value} в значении {@link Format#CUSTOM}.
     */
    String format() default "";

    /**
     * @return добавлять или нет миллисекунды.
     */
    boolean withMillis() default false;

    /**
     * Допустимые форматы.
     */
    enum Format {

        /**
         * ss.
         */
        SECONDS,

        /**
         * mm:ss
         */
        MINUTES,

        /**
         * HH:mm:ss
         */
        HOURS,

        CUSTOM
    }

}
