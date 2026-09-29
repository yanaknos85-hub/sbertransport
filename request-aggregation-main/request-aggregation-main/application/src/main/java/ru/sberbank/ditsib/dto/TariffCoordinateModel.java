package ru.sberbank.ditsib.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Доменная модель координаты для тарифа
 */
public record TariffCoordinateModel(
        @NotNull
        BigDecimal latitude,
        @NotNull
        BigDecimal longitude
) {
} 