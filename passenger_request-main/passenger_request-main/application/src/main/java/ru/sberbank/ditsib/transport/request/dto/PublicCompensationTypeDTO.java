package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(title = "Типы компенсации за поездки на общественном транспорте",
        description = "Типы компенсации за поездки на общественном транспорте")
public class PublicCompensationTypeDTO {
    @NotNull
    @Schema(description = "Наименование константы", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    @Schema(description = "Русскоязычное наименование для фронта")
    private String rusName;

    @Schema(description = "Необходимо приложить документы")
    private boolean attachmentDocumentRequired;

    @Schema(description = "Необходимо ввести даты действия билета")
    private boolean expirationDatesRequired;
}
