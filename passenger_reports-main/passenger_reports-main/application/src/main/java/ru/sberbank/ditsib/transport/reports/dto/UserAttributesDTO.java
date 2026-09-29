package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
@Schema(title = "Данные аттрибутов пользователя (чтение)", description = "Данные аттрибутов пользователя")
public class UserAttributesDTO {

    @Schema(description = "Идентификатор, первичный ключ", requiredMode = Schema.RequiredMode.REQUIRED)
    private final UUID id;

    @NotNull
    @Valid
    @Schema(description = "Видимость атрибутов пользователя для такси", requiredMode = Schema.RequiredMode.REQUIRED)
    private final TaxiUIVisibilityDTO taxiUIVisibility;

    @NotNull
    @Valid
    @Schema(description = "Видимость атрибутов пользователя для личного транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    private final PersonalUIVisibilityDTO personalUIVisibility;

    @NotNull
    @Valid
    @Schema(description = "Видимость атрибутов пользователя для общественного транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    private final PublicUIVisibilityDTO publicUIVisibility;

    @NotNull
    @Valid
    @Schema(description = " Видимость атрибутов пользователя для каршеринга")
    private final CarsharingUIVisibilityDTO carsharingUIVisibility;
}
