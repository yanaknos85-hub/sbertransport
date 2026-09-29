package ru.sber.transport.contractor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.database.model.ServiceType;
import ru.sber.transport.contractor.validation.annotation.IntegrationValidation;
import ru.sber.transport.contractor.validation.annotation.MsrnTinValidation;

import java.util.UUID;

/**
 * Объект с новыми данными контрагента.
 *
 * @param name                    название.
 * @param msrn                    ОГРН.
 * @param tin                     ИНН.
 * @param contactPersonFirstName  Имя контактного лица контрагента.
 * @param contactPersonLastName   Фамилия контактного лица контрагента.
 * @param contactPersonPatronymic Отчество контактного лица контрагента.
 * @param contactPersonPhone      телефон контактного лица.
 * @param contactPersonEmail      Электронная почта контактного лица контрагента.
 * @param rating                  рейтинг.
 * @param img                     путь к логотипу.
 * @param jsonIntegrationParams   параметры интеграции по API.
 * @param mainDispatcherId        идентификатор основного диспетчера.
 * @param serviceType             Тип предоставляемых услуг.
 * @param contractorType          Категория контрагента.
 */

@MsrnTinValidation
@IntegrationValidation
@Schema(title = "Информация о контрагенте", description = "Новые данные контрагента")
public record NewContractorDTO(

        @NotBlank
        @Schema(description = "Полное наименование", maxLength = 128)
        @Size(max = 128)
        String name,

        @NotNull
        @Pattern(regexp = "^(\\d{13}|\\d{15})$", message = "ОГРН должен состоять из 13 или 15 чисел")
        @Schema(description = "ОГРН, 13 знаков", pattern = "^(\\d{13}|\\d{15})$", minLength = 13, maxLength = 15)
        String msrn,

        @NotNull
        @Schema(description = "ИНН", minLength = 10, maxLength = 12)
        @Size(min = 10, max = 12)
        String tin,

        @Schema(description = "Информация о контактном лице контрагента, имя", maxLength = 250)
        @Size(max = 250)
        @NotNull
        String contactPersonFirstName,

        @Schema(description = "Информация о контактном лице контрагента, фамилия", maxLength = 250)
        @Size(max = 250)
        @NotNull
        String contactPersonLastName,

        @Schema(description = "Информация о контактном лице контрагента, отчество", maxLength = 250)
        @Size(max = 250)
        String contactPersonPatronymic,

        @Schema(description = "Контактный номер контрагента", minLength = 10)
        @NotNull
        @Size(min = 10)
        String contactPersonPhone,

        @NotNull
        @Schema(description = "Контактная почта контрагента")
        String contactPersonEmail,

        @Schema(description = "Идентификатор контактоного лица")
        UUID employeeId,

        @Schema(description = "Рейтинг контрагента, 0-500", minimum = "0", maximum = "500")
        @Min(0)
        @Max(500)
        Integer rating,

        @Schema(description = "URL лого контрагента")
        String img,

        @Schema(description = "Json Интеграционные параметры контрагента")
        JsonIntegrationParamsDto jsonIntegrationParams,

        @Schema(description = "Информация об основном диспетчере. Будет использоваться предложенный. В приоритете перед " +
                "`mainDispatcher`. Если диспетчер с указанным идентификатором не найден, но предоставлены сведения о " +
                "новом диспетчере, будет заведен новый диспетчер")
        UUID mainDispatcherId,

        @Schema(description = "Тип услуги")
        @NotNull
        ServiceType serviceType,

        @Schema(description = "Метод интеграции")
        @NotNull
        ContractorType contractorType,

        @Schema(description = "Нормативное количество автомобилей в автопарке")
        Integer vehicleCountNorm
) {
}
