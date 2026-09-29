package ru.sberbank.ditsib.transport.request.service.taxiprice.struct;

import java.util.List;

/**
 * DTO совместной поездки magenta
 */
public record YandexCalculateResponseDto(
        String currency,
        List<Option> options,
        Double time
) {
    public record Option(
            Integer class_level,
            String class_name,
            String class_text,
            Integer min_price,
            Integer price,
            String price_text,
            Double waiting_time
    ) {
    }
}

