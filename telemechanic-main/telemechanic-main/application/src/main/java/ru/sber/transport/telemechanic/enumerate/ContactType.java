package ru.sber.transport.telemechanic.enumerate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Тип контактных данных
 */
@Getter
@RequiredArgsConstructor
@Schema(title = "Тип контактных данных", description = "Тип контактных данных")
public enum ContactType {
    @Schema(description = "Телефон")
    PHONE,
    @Schema(description = "E-mail")
    EMAIL,
    @Schema(description = "Сайт")
    SITE
}