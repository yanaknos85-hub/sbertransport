package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.util.*;

/**
 * Объект с данными контрагента.
 *
 * @param name название.
 * @param msrn ОГРН.
 * @param tin ИНН.
 * @param contactPersonInfo информация о контактном лице.
 * @param contactPersonPhone телефон контактного лица.
 * @param rating рейтинг.
 * @param img путь к логотипу.
 * @param regionIds список обслуживаемых регионов.
 * @param integrationParams параметры интеграции.
 * @param digitId номер.
 */
@Schema(title = "Информация о контрагенте", description = "Новые данные контрагента")
public record NewContractorParameterDTO (

        @NotBlank
        @Schema(description = "Полное наименование")
        @Size(max = 128)
        String name,

        @NotNull
        @Pattern(regexp = "\\d{13}", message = "ОГРН должен состоять из 13 чисел")
        @Schema(description = "ОГРН, 13 знаков")
        String msrn,

        @NotNull
        @Schema(description = "ИНН 10 знаков")
        @Size(min = 10, max = 10)
        String tin,

        @Schema(description = "Информация о контактном лице контрагента", maxLength = 250)
        @Size(max = 250)
        String contactPersonInfo,

        @Schema(description = "Контактный номер контрагента")
        @Size(min = 10)
        String contactPersonPhone,

        @Schema(description = "Рейтинг контрагента, 0-500")
        @Min(0)
        @Max(500)
        Integer rating,

        @Schema(description = "URL лого контрагента")
        String img,

        @Schema(description = "Идентификаторы обслуживаемых регионов")
        List<UUID> regionIds,

        @Schema(description = "Интеграционные параметры контрагента")
        @Valid
        EmailIntegrationParamsDto integrationParams,

        @Schema(description = "человекочитаемый id")
        Long digitId

) {}
