package ru.sber.transport.trips.cargo.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Источник", description = "Источник заказа")
public enum RequestSource {
    SB,
    DZO
}
