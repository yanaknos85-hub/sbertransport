package ru.sberbank.ditsib.transport.constants;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Опыт вождения.
 */
@Schema(title = "Опыт вождения водителя", description = "Опыт вождения водителя")
public enum DrivingExperience {

    /**
     * 1 - 5 лет.
     */
    LESS_THEN_FIVE,

    /**
     * 5 - 10 лет.
     */
    FIVE_TO_TEN,

    /**
     * Более 10 лет.
     */
    MORE_THEN_TEN;

}
