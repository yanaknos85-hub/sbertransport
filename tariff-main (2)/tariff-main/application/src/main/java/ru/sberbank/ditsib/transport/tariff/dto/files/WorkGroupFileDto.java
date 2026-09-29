package ru.sberbank.ditsib.transport.tariff.dto.files;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Builder
@AllArgsConstructor
@Schema(title = "Параметры рабочей группы")
public class WorkGroupFileDto {
    @NotBlank
    @Schema(description = "Название организации")
    private final String organizationName;
    @NotBlank
    @Schema(description = "Название рабочей группы")
    private final String name;
    @NotBlank
    @Schema(description = "Название контрагента")
    private final String contractorName;
    @NotBlank
    @Schema(description = "Название геозоны")
    private final String geoZoneName;
    @NotBlank
    @Schema(description = "Идентификатор геозоны")
    private final String geoZoneId;
    @NotBlank
    @Schema(description = "EMAIL контрагента")
    private final String integrationEmail;
    @NotBlank
    @Schema(description = "№ Договора")
    private final String contractNumber;
}
