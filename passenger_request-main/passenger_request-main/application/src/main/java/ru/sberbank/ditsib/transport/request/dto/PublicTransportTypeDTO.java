package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(title = "Доступные типы общественного транспорта", description = "Доступные типы общественного транспорта")
public class PublicTransportTypeDTO {
    @NotNull
    @Schema(description = "Наименование константы", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    @Schema(description = "Русскоязычное наименование для фронта")
    private String rusName;

    @Schema(description = "Тип компенсации")
    private String publicCompensationType;
}