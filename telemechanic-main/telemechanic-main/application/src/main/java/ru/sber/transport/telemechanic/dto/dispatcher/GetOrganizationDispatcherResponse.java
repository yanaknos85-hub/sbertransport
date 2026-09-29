package ru.sber.transport.telemechanic.dto.dispatcher;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Schema(name = "GetOrganizationDispatcherResponse", title = "Ответ на запрос получения данных по организации диспетчера")
public record GetOrganizationDispatcherResponse(
        @Schema(description = "Идентификатор записи о диспетчере",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID dispatcherId,
        @Schema(description = "Код региона РФ, в котором действует диспетчер",
                requiredMode = Schema.RequiredMode.REQUIRED,
                pattern = "^\\d{2,3}$",
                minimum = "2",
                maximum = "3",
                example = "77")
        @Size(min = 2, max = 3)
        @Pattern(regexp = "^\\d{2,3}$")
        @NotNull
        @Positive
        String regionCode,
        @Schema(description = "Организация",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        OrganizationDto organization
) {
    
    @Schema(name = "GetOrganizationDispatcherResponse.OrganizationDto", title = "Организация")
    public record OrganizationDto(
            @Schema(description = "Идентификатор записи об организации",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull
            UUID id,
            @Schema(description = "Служебное название",
                    maximum = "255",
                    example = "ЦА")
            @Size(max = 255)
            String name,
            @Schema(description = "ОГРН",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "1027700132195")
            @NotBlank
            String msrn,
            @Schema(description = "ИНН",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "7707083893")
            @NotBlank
            String tin,
            @Schema(description = "Телефон",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "8 (800) 555-55-50")
            @NotBlank
            String phone
    ) {
    }
}