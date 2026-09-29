package ru.sberbank.ditsib.dto;

/**
 * Координаты точки (широта и долгота)
 */
public record CoordinateDto(
        double latitude,
        double longitude
) {
}

