package ru.sber.transport.dispatcher.dto;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.*;

import jakarta.validation.constraints.*;

/**
 * Объект обмена интеграционными параметрами.
 *
 * @param contractorName    латинское название контрагента.
 * @param contractorRusName название контрагента.
 * @param email             E-Mail
 */
@Schema(title = "Данные для интеграции по EMail", description = "Набор данных для проведения EMail-интеграции")
public record EmailIntegrationParamsDto(

        @NotNull
        @Size(min = 3)
        @Schema(title = "Латинское название контрагента")
        String contractorName,

        @NotNull
        @Size(min = 3)
        @Schema(title = "Название контрагента")
        String contractorRusName,

        @NotBlank
        @Schema(title = "EMail")
        @Email
        @JsonProperty("integrationEmail")
        @JsonAlias("email")
        String email
) {

}
