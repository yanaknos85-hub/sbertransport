package ru.sber.transport.dispatcher.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.lang.Nullable;
import ru.sber.transport.dispatcher.dto.enums.DriverSpecialityType;
import ru.sberbank.ditsib.transport.constants.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.*;

import static ru.sber.transport.dispatcher.consts.DateConstants.DDMMYYYY;

/**
 * DTO - Информация о водителе
 */
@Schema(title = "Информация о водителе", description = "Данные водителя")
public record DriverDTO(

        @NotNull
        @Schema(description = "ID водителя")
        UUID id,

        @NotNull
        @Schema(description = "Человекочитаемый идентификатор")
        String humanReadableId,

        @NotBlank
        @Size(max = 20, min = 2)
        @Schema(description = "Имя", minLength = 2, maxLength = 20)
        String firstName,

        @NotBlank
        @Size(max = 40, min = 2)
        @Schema(description = "Фамилия", minLength = 2, maxLength = 40)
        String lastName,

        @Schema(description = "Отчество")
        String patronymic,

        @Schema(description = "Серия номер паспорта, через пробел", pattern = "(\\d4\\s\\d6)")
        String passport,

        @NotBlank
        @Schema(description = "Контактный номер телефона", pattern = "8\\([0-9]{3}\\)[0-9]{7}")
        @Pattern(regexp = "(\\+7)\\(\\d{3}\\)\\d{7}", message = "Формат должен быть 8(999)9999999")
        String contactPhone,

        @Schema(description = "Рейтинг водителя 0-500", minimum = "0", maximum = "500")
        @Min(0)
        @Max(500)
        int rating,

        @Schema(description = "Номер водительского удостоверения")
        @Pattern(regexp = "^(\\d\\s*){10}$", message = "Номер удостоверения состоит из 10 цифр")
        String driverLicenseNumber,

        @Size(max = 12, min = 8)
        @Nullable
        @Schema(description = "Номер лиценции о предоставлении услуг", minLength = 8, maxLength = 12)
        String serviceLicenseNumber,

        @Size(max = 12, min = 8)
        @Nullable
        @Schema(description = "Номер лиценции о предоставлении услуг по грузовым перевозкам", minLength = 8, maxLength = 12)
        String cargoLicenceNumber,

        @Schema(description = "Опыт вождения")
        DrivingExperience experience,

        @Schema(description = "Категории прав ТС")
        Set<DriverLicenseDto> driverLicenses,

        @Schema(description = "Список признаков водителя")
        Set<@Valid AttributeDTO> attributes,

        @Schema(description = "Текущая смена водителя")
        UUID currentShift,

        @Schema(description = "ID контрагента")
        UUID contractorId,

        @Schema(description = "Признак выхода на линию")
        boolean online,

        @Schema(description = "Почта")
        String email,

        @Schema(description = "Признак подписания ПДн")
        boolean consent,

        @Schema(description = "Признак активности")
        boolean active,

        @NotNull
        @Schema(description = "Специализация водителя")
        DriverSpecialityType driverSpeciality,

        @Schema(title = "Подтверждение номера телефона")
        boolean phoneConfirmed,

        @Schema(title = "Идентификатор филиала")
        UUID autoparkId,

        @Schema(title = "Идентификатор организации")
        String autoparkName,

        @Schema(title = "СНИЛС")
        String snils,

        @Schema(title = "ИНН")
        String tin,

        @Schema(description = "Дата выдачи")
        @JsonFormat(pattern = DDMMYYYY)
        LocalDate issueDate,

        @Schema(description = "Дата окончания срока действия")
        @JsonFormat(pattern = DDMMYYYY)
        LocalDate expiryDate,

        @Schema(title = "Табельный номер")
        String personnelNumber


) implements HasName {
}
