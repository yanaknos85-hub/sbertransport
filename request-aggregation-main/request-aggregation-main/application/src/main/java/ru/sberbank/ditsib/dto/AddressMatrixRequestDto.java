package ru.sberbank.ditsib.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Матрица расстояний между адресами для основного сервиса
 * (данные от 2ГИС через GEO)
 */
public record AddressMatrixRequestDto(
        @Size(max = 255)
        @NotBlank
        String address1,
        @Size(max = 255)
        @NotBlank
        String address2,
        Integer distanceWithPoints,
        Integer timeSec
) {
}
