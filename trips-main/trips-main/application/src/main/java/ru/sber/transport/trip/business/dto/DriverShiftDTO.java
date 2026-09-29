package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.DrivingExperience;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Объект обмена данными о водителе, его текущей смене и удаленности от места начала поездки")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverShiftDTO {

    @NotNull
    @Schema(description = "ID водителя")
    private UUID id;

    @NotNull
    @Schema(description = "Человекочитаемый идентификатор")
    private String humanReadableId;


    @NotBlank
    @Size(max = 20, min = 2)
    @Schema(description = "Имя", minLength = 2, maxLength = 20)
    private String firstName;

    @NotBlank
    @Size(max = 40, min = 2)
    @Schema(description = "Фамилия", minLength = 2, maxLength = 40)
    private String lastName;

    @Schema(description = "Отчество")
    private String patronymic;

    @NotBlank
    @Schema(description = "Серия номер паспорта, через пробел", pattern = "(\\d4\\s\\d6)")
    private String passport;

    @NotBlank
    @Schema(description = "Контактный номер телефона", pattern = "8\\([0-9]{3}\\)[0-9]{7}")
    @Pattern(regexp = "(\\+7)\\(\\d{3}\\)\\d{7}", message = "Формат должен быть 8(999)9999999")
    private String contactPhone;

    @Schema(description = "Рейтинг водителя 0-500", minimum = "0", maximum = "500")
    @Min(0)
    @Max(500)
    private int rating;

    @Schema(description = "Номер водительского удостоверения")
    @Pattern(regexp = "^(\\d\\s*){10}$", message = "Номер удостоверения состоит из 10 цифр")
    private String driverLicenseNumber;

    @Size(max = 12, min = 8)
    @Schema(description = "Номер лиценции о предоставлении услуг", minLength = 8, maxLength = 12)
    private String serviceLicenseNumber;

    @Schema(description = "Опыт вождения")
    private DrivingExperience experience;

    @Schema(description = "ID текущей смены водителя")
    private UUID shiftId;

    @Schema(description = "Текущая смена водителя")
    private ShiftResponseDTO currentShift;

    @Schema(description = "Широта")
    private Double latitude;

    @Schema(description = "Долгота")
    private Double longitude;

    @Schema(description = "Расстояние до места отправления")
    private Double distanceInKilometer;

    @Schema(description = "Азимут")
    private Double azimuth;

    @Schema(description = "ID активной поездки")
    private UUID activeTripId;
}
