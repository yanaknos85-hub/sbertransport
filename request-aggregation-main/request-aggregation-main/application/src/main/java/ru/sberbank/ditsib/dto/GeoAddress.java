package ru.sberbank.ditsib.dto;

import java.math.BigDecimal;

/**
 * Гео-Адрес полученный по grpc
 */
public record GeoAddress(

        String country,
        String region,
        String city,
        String street,
        String house,
        String building,
        String structure,
        BigDecimal latitude,
        BigDecimal longitude
) {
}

