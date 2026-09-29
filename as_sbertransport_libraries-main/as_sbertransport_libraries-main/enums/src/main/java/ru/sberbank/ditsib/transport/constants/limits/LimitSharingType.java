package ru.sberbank.ditsib.transport.constants.limits;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Типы распределений лимита.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum LimitSharingType {

    /**
     * Месячное распределение.
     */
    MONTHLY(12),

    /**
     * Квартальное распределение.
     */
    QUARTER(4),

    /**
     * Процентное распределение.
     */
    PERCENTS(12);
    
    private final Integer numOfPeriods;
}