package ru.sber.transport.constants.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Object describes types of public compensations.
 *
 * @param name name of type.
 * @param rusName localized name of type.
 * @param attachmentDocumentRequired flag of requiring of attachment.
 * @param expirationDatesRequired flag of requiring of expiration dates.
 */
@Schema(title = "Типы компенсации за поездки на общественном транспорте",
    description = "Типы компенсации за поездки на общественном транспорте")
public record PublicCompensationTypeDTO(

    @NotNull
    @Schema(description = "Наименование константы") String name,

    @Schema(description = "Русскоязычное наименование для фронта") String rusName,

    @Schema(description = "Необходимо приложить документы") boolean attachmentDocumentRequired,

    @Schema(description = "Необходимо ввести даты действия билета") boolean expirationDatesRequired
) {
}
