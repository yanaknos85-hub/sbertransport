package ru.sber.transport.contractor.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Метод интеграции", description = "Метод интеграции")
public record EnumRusNameDTO(String name, String rusName) {
}
