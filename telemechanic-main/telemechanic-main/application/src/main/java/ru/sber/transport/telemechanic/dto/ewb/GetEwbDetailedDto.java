package ru.sber.transport.telemechanic.dto.ewb;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(name = "GetEwbDetailedDto", title = "Детальная информация карточке ЭПЛ")
public record GetEwbDetailedDto(
    @Schema(description = "Человекочитаемый идентификатор заявки",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1234567890")
    String humanReadableId,
    @Schema(description = "Дата формирования путевого листа",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2025-07-18")
    LocalDate creationDate,
    @Schema(description = "Дата начала действия ЭПЛ",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2025-07-18")
    LocalDate startDate,
    @Schema(description = "Дата окончания действия ЭПЛ",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2025-07-18")
    LocalDate finishDate,
    @Schema(description = "ОГРН Организации",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2025-07-18")
    String msrn,
    @Schema(description = "ИНН организации",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "192706790650")
    String tin,
    @Schema(description = "Номер контактного телефона",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "+74543534534")
    String phoneNumber,
    @Schema(description = "Название организации",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "ПАО Сбербанк")
    String organizationName,
    @Schema(description = "Статус",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "На линии")
    String status,
    @Schema(description = "ФИО ответственного за выпуск ТС",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Иванов И.И.")
    String dispatcherFullName,
    @Schema(description = "Вид перевозки",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "")
    String transportationType,
    @Schema(description = "Вид сообщения",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "")
    String communicationType,
    @Schema(description = "Автомобиль",
            requiredMode = Schema.RequiredMode.REQUIRED)
    Transport transport,
    @Schema(description = "Водитель",
            requiredMode = Schema.RequiredMode.REQUIRED)
    Driver driver,
    @Schema(description = "Водительское удостоверение",
            requiredMode = Schema.RequiredMode.REQUIRED)
    DrivingLicense drivingLicense,
    @Schema(description = "Медосмотр на выезде",
            requiredMode = Schema.RequiredMode.REQUIRED)
    Medic medic,
    @Schema(description = "Тех.осмотр на выезде",
            requiredMode = Schema.RequiredMode.REQUIRED)
    TelemechOut telemechOut
) {
        
        @Schema(name = "GetEwbDetailedDto.Transport", title = "Транспорт")
        public record Transport(
                @NotBlank
                @Size(max = 255)
                @Schema(description = "Марка",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "Lada",
                        maximum = "255")
                String brand,
                @NotBlank
                @Size(max = 255)
                @Schema(description = "Модель",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "Kalina",
                        maximum = "255")
                String model,
                @NotBlank
                @Schema(description = "Государственный номер",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        pattern = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                        example = "A123AA777")
                String stateNumber
        ) {
        }
        
        @Schema(name = "GetEwbDetailedDto.Driver", title = "Водитель")
        public record Driver(
                @NotBlank
                @Schema(description = "Фамилия",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "Иван")
                String firstName,
                @NotBlank
                @Schema(description = "Фамилия",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "Иванов")
                String lastName,
                @NotBlank
                @Schema(description = "Фамилия",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "Иванович")
                String patronymic,
                @NotBlank
                @Schema(description = "Телефон",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "+74543534534")
                String mobilePhone,
                @NotBlank
                @Schema(description = "ИНН водителя",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "192706790650")
                String tin
        ) {
        }
        
        @Schema(name = "GetEwbDetailedDto.DrivingLicense", title = "Водительское удостоверение")
        public record DrivingLicense(
                @Size(min = 1, max = 20, message = "Серия водительских прав должна быть не менее 1 и не более 20 символов")
                @Schema(description = "Серия водительского удостоверения",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        example = "7708",
                        minimum = "1",
                        maximum = "20",
                        nullable = true)
                String series,
                @Size(min = 1, max = 20, message = "Номер водительского удостоверения должен быть не менее 1 и не более 20 символов")
                @Schema(description = "Номер водительского удостоверения",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        example = "203040",
                        minimum = "1",
                        maximum = "20",
                        nullable = true)
                String number
        ) {
        }
        
        @Schema(name = "GetEwbDetailedDto.Medic", title = "Медосмотр на выезде")
        public record Medic(
                @Schema(description = "Статус",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "К работе на линии допущен")
                String status,
                @Schema(description = "Фамилия и инициалы",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "Иванов И.И.")
                String fullName,
                @JsonSerialize(using = LocalDateTimeMillisConverter.class)
                @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
                @Schema(description = "Дата и время медицинского осмотра",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        type = "integer",
                        example = "1688118467098")
                LocalDateTime decisionTime
        ) {
        }
        
        @Schema(name = "GetEwbDetailedDto.TelemechOut", title = "Тех.осмотр на выезде")
        public record TelemechOut(
                @Schema(description = "Статус",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "")
                String status,
                @Schema(description = "Фамилия и инициалы",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "Иванов И.И.")
                String fullName,
                @JsonSerialize(using = LocalDateTimeMillisConverter.class)
                @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
                @Schema(description = "Дата и время техосмотра осмотра",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        type = "integer",
                        example = "1688118467098")
                LocalDateTime decisionTime,
                @Schema(description = "Показания одометра при выезде",
                        example = "100000",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                Integer mileage
        ) {
        }
}
