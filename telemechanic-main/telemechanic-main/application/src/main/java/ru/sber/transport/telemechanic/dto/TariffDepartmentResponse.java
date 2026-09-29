package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "TariffDepartmentResponse", title = "Департаменты с активным тарифом", description = "Департаменты, у которых есть активный тариф")
public record TariffDepartmentResponse(
        @Schema(description = "Идентификатор департамента",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @Schema(description = "Наименование департамента",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Департамент А")
        String departmentName,
        @Schema(description = "Идентификатор родительского департамента")
        UUID parentId
) {
}
