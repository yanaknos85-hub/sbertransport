package ru.sberbank.ditsib.transport.tariff.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Виды НДС
 */
@AllArgsConstructor
@Getter
public enum VatType {

    /**
     * Базовый c 2026, 22%
     */
    BASE_22("22%", 22),

    /**
     * Уменьшенный, 10%
     */
    BENEFITED("10%", 10),

    /**
     * Уменьшенный, 7%
     */
    REDUCED_SEVEN("7%", 7),

    /**
     * Уменьшенный, 5%
     */
    REDUCED_FIVE("5%", 5),

    /**
     * Нулевой, 0%
     */
    ZERO("0%", 0),

    /**
     * Без НДС, null
     */
    WITHOUT_VAT("Без НДС", null);

    private final String verboseName;
    private final Integer percentValue;
}
