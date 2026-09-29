package ru.sber.transport.contractor.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Объект с данными контрагента.
 *
 * @param id идентификатор.
 */
@Schema(title = "Информация о контрагенте", description = "Данные контрагента")
public record DispatcherResponseDTO(


        @NotNull
        @Schema(description = "Идентификатор")
        UUID id

) {
}