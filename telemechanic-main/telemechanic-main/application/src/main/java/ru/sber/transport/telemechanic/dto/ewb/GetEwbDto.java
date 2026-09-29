package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Accessors(chain = true)
@Schema(title = "Данные для карточки ЭПЛ")
public class GetEwbDto {
    @NotNull
    @Schema(description = "ID записи")
    private UUID id;
    @Schema(description = "Человекочитаемый идентификатор заявки")
    @NotNull
    private String humanReadableId;
    @NotNull
    @Schema(description = "UUID ГИС ЭПД")
    private String ewbUuid;
    @Schema(description = "Вид перевозки")
    private String transportationType;
    @Schema(description = "Подвид перевозки")
    private String transportationSubtype;
    @Schema(description = "Вид сообщения")
    private String communicationType;
    @NotNull
    @Schema(description = "Дата создания путевого листа")
    private LocalDate creationDate;
    @NotNull
    @Schema(description = "Дата путевого листа")
    private LocalDate startDate;
    @NotNull
    @Schema(description = "Дата окончания путевого листа")
    private LocalDate finishDate;
    @NotNull
    @Schema(description = "Субъект Российской Федерации (код)")
    private String regionCode;
    @NotBlank
    @Schema(description = "ОГРН Организации")
    private String msrn;
    @NotBlank
    @Schema(description = "ИНН организации")
    private String tin;
    @Schema(description = "Номер контактного телефона")
    private String phoneNumber;
    @NotNull
    @Schema(description = "Название организации")
    private String organizationName;
    @NotNull
    @Schema(description = "Статус")
    private EwbStatus status;
    @NotNull
    @Schema(description = "Автомобиль")
    private Transport transport;
    @NotNull
    @Schema(description = "Водитель")
    private Driver driver;
    @NotNull
    @Schema(description = "Водительские права")
    private DrivingLicense drivingLicense;
    @NotNull
    @Schema(description = "Ответственный за выпуск ТС")
    private Author author;
    @Schema(description = "Медосмотр на выезде")
    private Medic medic;
    @Schema(description = "Техосмотр на выезде")
    private Telemech telemechOut;
    @Schema(description = "Техосмотр на заезде")
    private Telemech telemechIn;
    
    @Getter
    @Setter
    @AllArgsConstructor
    @Accessors(chain = true)
    @Schema(title = "Автомобиль")
    public static class Transport {
        @NotNull
        @Schema(description = "Марка")
        private String brand;
        @NotNull
        @Schema(description = "Модель")
        private String model;
        @NotNull
        @Schema(description = "Государственный номер")
        private String stateNumber;
        @NotNull
        @Schema(description = "Тип транспортного средства")
        private String transportType;
        @PositiveOrZero
        @Schema(description = "Пробег при выезде",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "53453")
        private Integer odometerOut;
        @PositiveOrZero
        @Schema(description = "Остаток топлива при выезде",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "20")
        private Integer fuelLitreageOut;
        @NotNull
        @Positive
        @Schema(description = "Объем топливного бака",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "60")
        private int fuelTankVolume;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(title = "Водитель")
    public static class Driver {
        @NotNull
        @Schema(description = "Имя")
        private String firstName;
        @NotNull
        @Schema(description = "Фамилия")
        private String lastName;
        @Schema(description = "Отчество")
        private String patronymic;
        @NotNull
        @Schema(description = "Табельный номер водителя")
        private String personnelNumber;
        @Schema(description = "Телефон")
        private String mobilePhone;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(title = "Водительские права")
    public static class DrivingLicense {
        @NotNull
        @Schema(description = "Серия")
        private String series;
        @NotNull
        @Schema(description = "Номер")
        private String number;
        @NotNull
        @Schema(description = "Дата выдачи прав")
        private LocalDate issueDate;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(title = "Ответственный за выпуск ТС")
    public static class Author {
        @NotNull
        @Schema(description = "Имя")
        private String firstName;
        @NotNull
        @Schema(description = "Фамилия")
        private String lastName;
        @Schema(description = "Отчество")
        private String patronymic;
        @Schema(description = "Телефон")
        private String mobilePhone;
        @NotNull
        @Schema(description = "Табельный номер")
        private String personnelNumber;
        @NotNull
        @Schema(description = "МЧД")
        private Attorney attorney;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(title = "МЧД")
    public static class Attorney {
        @NotNull
        @Schema(description = "Номер доверенности")
        private UUID attorneyNumber;
        @NotNull
        @Schema(description = "Дата доверенности")
        private LocalDate issueDate;
        @NotNull
        @Schema(description = "Система, в которой осуществляется хранение доверенности")
        private String creationSystem;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(title = "Медосмотр на выезде")
    public static class Medic {
        @NotNull
        @Schema(description = "Имя")
        private String firstName;
        @NotNull
        @Schema(description = "Фамилия")
        private String lastName;
        @Schema(description = "Отчество")
        private String patronymic;
        @NotNull
        @Schema(description = "Должность")
        private String position;
        @Schema(description = "Медосмотр пройден")
        private boolean medicSuccess;
        @Schema(description = "Дата и время медицинского осмотра")
        private LocalDateTime decisionTime;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(title = "Техосмотр")
    public static class Telemech {
        @NotNull
        @Schema(description = "Имя")
        private String firstName;
        @NotNull
        @Schema(description = "Фамилия")
        private String lastName;
        @Schema(description = "Отчество")
        private String patronymic;
        @Schema(description = "Должность")
        private String position;
        @Schema(description = "Техосмотр пройден")
        private boolean telemechSuccess;
        @Schema(description = "Дата и время техосмотра осмотра")
        private LocalDateTime decisionTime;
        @Schema(description = "Показания одометра")
        private int mileage;
    }
}
