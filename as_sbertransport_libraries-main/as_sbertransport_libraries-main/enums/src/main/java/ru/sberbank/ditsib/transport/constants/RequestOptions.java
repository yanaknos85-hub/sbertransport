package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Параметры заявки.
 */
@Getter
@RequiredArgsConstructor
public enum RequestOptions {

    /**
     * Требуется детское кресло.
     */
    CHILD_SEAT("Детское кресло"),

    /**
     * Животные.
     */
    PET_TRANSPORTATION("Перевозка животного"),

    /**
     * Негабарит (лыжи, сноуборд, вело и пр.)
     */
    BICYCLE_SKI_TRANSPORTATION("Перевозка лыж/сноуборда/велосипеда и т.д."),

    /**
     * Некурящий водитель
     */
    NON_SMOKING_DRIVER("Некурящий водитель"),

    /**
     * Желтые номера
     */
    YELLOW_REG_PLATES("Желтые номера"),

    /**
     * ТМЦ
     */
    MATERIAL_ASSETS_TRANSPORTATION("Перевозка ТМЦ"),

    /**
     * Личные цели
     */
    PERSONAL_USAGE("Использование в личных целях");
    
    private final String description;
}
