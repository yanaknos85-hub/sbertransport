package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.*;

import java.util.*;

/**
 * Данные диспетчера.
 *
 * @param id идентификатор.
 * @param lastName фамилия.
 * @param firstName имя.
 * @param patronymic отчество.
 */
@Schema(title = "Диспетчер",
        description = "Данные диспетчера")
public record DispatcherSelectDto(

        @Schema(title = "Идентификатор")
        UUID id,

        @Schema(title = "Фамилия")
        String lastName,

        @Schema(title = "Имя")
        String firstName,

        @Schema(title = "Отчество")
        String patronymic

) implements HasName {
}
