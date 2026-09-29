package ru.sber.transport.dispatcher.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.lang.Nullable;
import ru.sber.transport.dispatcher.dto.enums.DriverSpecialityType;
import ru.sber.transport.dispatcher.validation.ValidationConstants;
import ru.sberbank.ditsib.transport.constants.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.*;

import static ru.sber.transport.dispatcher.consts.DateConstants.DDMMYYYY;

/**
 * DTO - Информация о новом водителе
 */
@Schema(title = "Информация о новом водителе", description = "Новые данные водителя")
public record NewDriverDTO (

        @NotBlank
        @Schema(description = "Имя")
        @Pattern(regexp = "^[a-zA-ZА-ЯЁа-яё]+$")
        String firstName,

        @NotBlank
        @Schema(description = "Фамилия", minLength = 1, maxLength = 50)
        @Pattern(regexp = "^[a-zA-ZА-ЯЁа-яё\\-]{1,50}$")
        String lastName,

        @Schema(description = "Отчество")
        @Pattern(regexp = "^[-a-zA-ZА-ЯЁа-яё\\s]+$")
        String patronymic,

        @Schema(description = "Серия номер паспорта, через пробел", pattern = "(\\d4\\s\\d6)")
        String passport,

        @NotBlank
        @Schema(description = "Контактный номер телефона", pattern = "+7\\([0-9]{3}\\)[0-9]{7}")
        @Pattern(regexp = "(\\+7)\\(?\\d{3}\\)?\\d{7}", message = "Формат должен быть +7(999)9999999")
        String contactPhone,

        @Schema(description = "Рейтинг водителя 0-500", minimum = "0", maximum = "500")
        @Min(0)
        @Max(500)
        int rating,

        @Schema(description = "Номер водительского удостоверения")
        @Pattern(regexp = "^\\d{2}\\s\\d{2}\\s\\d{6}$", message = "Номер удостоверения состоит из 10 цифр")
        String driverLicenseNumber,

        @Size(max = 12, min = 8)
        @Nullable
        @Schema(description = "Номер лиценции о предоставлении услуг по пассажирским перевозкам", minLength = 8, maxLength = 12)
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

        @NotBlank
        @Email(regexp = ValidationConstants.EMAIL_REGEX)
        @Schema(title = "E-Mail", pattern = ValidationConstants.EMAIL_REGEX)
        String email,

        @Schema(description = "Признак активности")
        boolean active,

        @NotNull
        @Schema(description = "Специализация водителя")
        DriverSpecialityType driverSpeciality,

        @Schema(description = "Идентификатор водителя во внешней системе")
        UUID oauthId,

        @Schema(description = "Идентификатор филиала")
        UUID autoparkId,

        @Schema(description = "Табельный номер")
        String personnelNumber,

        @Schema(description = "СНИЛС")
        String snils,

        @Schema(description = "ИНН")
        String tin,

        @Schema(description = "Дата выдачи")
        @JsonFormat(pattern = DDMMYYYY)
        LocalDate issueDate,

        @Schema(description = "Дата окончания срока действия")
        @JsonFormat(pattern = DDMMYYYY)
        LocalDate expiryDate
) {

}
