package ru.sber.transport.contractor.dto;

import io.swagger.v3.oas.annotations.media.*;

import jakarta.validation.constraints.*;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.database.model.ServiceType;

import java.util.*;

/**
 * Объект с данными контрагента.
 *
 * @param name название.
 * @param msrn ОГРН.
 * @param tin ИНН.
 * @param contactPersonFirstName Имя контактного лица контрагента.
 * @param contactPersonLastName Фамилия контактного лица контрагента.
 * @param contactPersonPatronymic Отчество контактного лица контрагента.
 * @param contactPersonPhone телефон контактного лица.
 * @param contactPersonEmail Электронная почта контактного лица контрагента.
 * @param rating рейтинг.
 * @param img путь к логотипу.
 * @param integrationParams параметры интеграции для EMAIL XML.
 * @param digitId номер.
 * @param id идентификатор.
 * @param humanReadableId человекочитаемый идентификатор.
 * @param mainDispatcherId идентификатор основного диспетчера.
 * @param jsonIntegrationParams интеграционные параметры для API.
 * @param integrationType тип интеграции.
 * @param serviceType Тип предоставляемых услуг.
 * @param contractorType Категория контрагента.
 */
@Schema(title = "Информация о контрагенте", description = "Данные контрагента")
public record ContractorDTO(

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

        @Schema(description = "Информация о контактном лице")
        String contactPersonInfo,

        @Schema(description = "Информация о контактном лице контрагента, имя", maxLength = 250)
        @Size(max = 250)
        String contactPersonFirstName,

        @Schema(description = "Информация о контактном лице контрагента, фамилия", maxLength = 250)
        @Size(max = 250)
        String contactPersonLastName,

        @Schema(description = "Информация о контактном лице контрагента, отчество", maxLength = 250)
        @Size(max = 250)
        String contactPersonPatronymic,

        @Schema(description = "Контактный номер контрагента")
        @Size(min = 10)
        String contactPersonPhone,

        @NotNull
        @Schema(description = "Контактная почта контрагента")
        String contactPersonEmail,

        @Schema(description = "Рейтинг контрагента, 0-500")
        @Min(0)
        @Max(500)
        Integer rating,

        @Schema(description = "URL лого контрагента")
        String img,

        @Schema(description = "Интеграционные параметры контрагента для EMAIL XML")
        EmailIntegrationParamsDto integrationParams,

        @Schema(description = "Интеграционные параметры контрагента для API")
        JsonIntegrationParamsDto jsonIntegrationParams,

        @Schema(description = "человекочитаемый id")
        Long digitId,

        @NotNull
        @Schema(description = "Идентификатор")
        UUID id,

        @NotNull
        @Schema(description = "Человекочитаемый Идентификатор")
        String humanReadableId,

        @NotNull
        @Schema(description = "Тип интеграции")
        IntegrationTypeDto integrationType,

        @Schema(description = "Идентификатор основного диспетчера")
        UUID mainDispatcherId,

        @Schema(description = "Флаг включенности автоназначения")
        boolean autoassign,

        @Schema(description = "Тип услуги")
        ServiceType serviceType,

        @Schema(description = "Метод интеграции")
        ContractorType contractorType,

        @Schema(description = "Список организаций")
        Set<UUID> organizations,

        @Schema(description = "Идентификатор контрагента во внешней системе")
        UUID externalId,

        @Schema(description = "Нормативное количество автомобилей в автопарке")
        Integer vehicleCountNorm

) { }