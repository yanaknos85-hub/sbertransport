package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Schema(
        title = "Информация о реестре поездок на такси от контрагента, который необходимо импортировать",
        description = "Информация о реестре поездок на такси от контрагента, который необходимо импортировать"
)
@Builder
public class NewTaxiTripRegistryDTO {
    
    /**
     * ID Контрагента
     */
    @NotNull
    @Schema(description = "ID Контрагента", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID contractorId;
    
    /**
     * Дата реестра. Число в дате может быть любым
     */
    @NotNull
    @Schema(description = "Дата реестра. Число в дате может быть любым", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;
}
