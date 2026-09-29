package ru.sber.transport.trips.cargo.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Тип точки", description = "Тип точки (сбор/доставка/сбор-доставка)")
public enum WaypointType {

    LOAD("Сбор"),

    UNLOAD("Доставка"),

    LOAD_UNLOAD("Сбор и доставка");

    private final String description;

    WaypointType(String description) {
        this.description = description;
    }
}
