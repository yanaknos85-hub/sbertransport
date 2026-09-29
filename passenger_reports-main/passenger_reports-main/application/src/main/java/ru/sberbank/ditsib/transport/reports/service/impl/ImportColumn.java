package ru.sberbank.ditsib.transport.reports.service.impl;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Импортируемый столбец.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ImportColumn {

    /**
     * Индекс столбца.
     */
    String INDEX = "index";

    /**
     * Название столбца.
     */
    String NAME = "name";

    /**
     * @return имя столбца.
     */
    String name() default "";

    /**
     * @return индекс столбца.
     */
    int value();

}


