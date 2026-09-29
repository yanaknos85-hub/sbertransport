package ru.sber.transport.request.messaging;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.Length;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;

/**
 * Параметры интеграции.
 */
@Getter
@Builder
@Jacksonized
@Schema(title = "Интеграционные параметры контрагента", description = "Интеграционные параметры контрагента")
public class IntegrationParamsDTO {
    @Builder.Default
    @Length(min = 3)
    @Schema(description = "Название контрагента латиницей (указывается при интеграции в имени файла)",
            minLength = 3, maxLength = 100)
    private String contractorName = "yandex";
    
    @Builder.Default
    @Length(min = 3)
    @Schema(description = "Название контрагента кириллицей (указывается при интеграции в поле ИСПОЛНИТЕЛЬ)",
            minLength = 3, maxLength = 100)
    private String contractorRusName = "ЯНДЕКСТАКСИ";
    
    @Builder.Default
    @Pattern(regexp = "(?:[a-z\\d!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z\\d!#$%&'*+/=?^_`{|}~-]+)*|\"" +
                      "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z\\d](?:[a-z\\d-]*[a-z\\d])?\\.)+[a-z\\d](?:[a-z\\d-]*[a-z\\d])?|\\[(?:(?:(2(5[0-5]|[0-4][\\d])|1[\\d][\\d]|[1-9]?[\\d]))\\.){3}(?:(2(5[0-5]|[0-4][\\d])|1[\\d][\\d]|[1-9]?[\\d])|[a-z\\d-]*[a-z\\d]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
    @Length(min = 4, max = 255)
    @Schema(description = "Электронная почта, используемая для почтовой интеграции",
            minLength = 4, maxLength = 255,
            pattern = "(?:[a-z\\d!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z\\d!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z\\d](?:[a-z\\d-]*[a-z\\d])?\\.)+[a-z\\d](?:[a-z\\d-]*[a-z\\d])?|\\[(?:(?:(2(5[0-5]|[0-4][\\d])|1[\\d][\\d]|[1-9]?[\\d]))\\.){3}(?:(2(5[0-5]|[0-4][\\d])|1[\\d][\\d]|[1-9]?[\\d])|[a-z\\d-]*[a-z\\d]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
    private String integrationEmail = "robot-sber-test@yandex-team.ru";

    @Schema(description = "ИНН контрагента")
    private String tin;

    @Schema(description = "URL системы контрагента")
    private String contractorUrl;

    @Schema(description = "Login для авторизации в системе контрагента")
    private String contractorLogin;

    @Schema(description = "Password для авторизации в системе контрагента")
    private String contractorPassword;

    @Schema(description = "Тип интеграции")
    private TaxiExternalIntegrationType integrationType;
}
