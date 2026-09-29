package ru.sberbank.ditsib.transport.validation.annotation;

import ru.sberbank.ditsib.transport.validation.RangeConstraintValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Аннотация для валидации: сравнение границ диапазона, левая граница которого должна быть больше или равна правой
 */
@Documented
@Constraint(validatedBy = RangeConstraintValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Range.List.class)
public @interface Range {

    /**
     * @return сообщение невалидного значения.
     */
    String message() default "First Number is less then Second";

    /**
     * @return группы валидации.
     */
    Class<?>[] groups() default {};

    /**
     * @return полезная нагрузка.
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * @return название поля начала интервала.
     */
    String from();

    /**
     * @return название поля окончания интервала.
     */
    String to();

    /**
     * @return исключать ли границы валидации.
     */
    boolean exclude() default false;

    /**
     * Аннотация для обеспечения возможности указывать несколько {@link Range} на типе.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {

        /**
         * @return массив аннотаций.
         */
        Range[] value();
    }
}

    


