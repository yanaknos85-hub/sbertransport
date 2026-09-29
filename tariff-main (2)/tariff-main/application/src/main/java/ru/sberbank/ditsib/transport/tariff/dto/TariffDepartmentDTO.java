package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * DTO с данными по подразделению, на которое заводится тариф
 */
@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Данные по подразделению", description = "Данные по подразделению")
public class TariffDepartmentDTO {
    
    @Schema(description = "Идентификатор подразделения", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    @Schema(description = "Человекочитаемый идентификатор подразделения", requiredMode = Schema.RequiredMode.REQUIRED)
    private String humanReadableId;
    
    @Schema(description = "Код подразделения", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
