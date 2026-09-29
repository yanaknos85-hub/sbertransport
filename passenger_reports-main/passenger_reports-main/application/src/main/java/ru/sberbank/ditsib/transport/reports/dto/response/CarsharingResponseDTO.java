package ru.sberbank.ditsib.transport.reports.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.ReportResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.TariffShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonPropertyOrder({ "id" })
@Schema(title = "Информация", description = "Информация о заказе каршеринга")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CarsharingResponseDTO implements ReportResponseDTO {
    @Schema(description = "Идентификатор")
    private UUID id;
    
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
    
    @Schema(description = "Место возникновения затрат")
    private String costCenter;
    
    @Schema(description = "Наименование организация")
    private String organization;
    
    @Schema(description = "Наименование контрагента")
    private String contractor;
    
    @Schema(description = "Номер заказа")
    private String rentId;
    
    @Schema(description = "ФИО пассажира")
    private String fio;
    
    @Schema(description = "Табельный номер")
    private String personnelNumber;
    
    @Schema(description = "Должность")
    private String position;
    
    @Schema(description = "Телефон, который использовался при формирования заявки на каршеринг")
    private String phoneNumber;
    
    @Schema(description = "Цель поездки")
    private TripPurposeDTO purpose;
    
    @Schema(description = "Временная зона")
    private String timeZone;
    
    @Schema(description = "Статус заявки")
    private TripRequestStatus status;
    
    @Schema(description = "Время создания заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime creationTime;
    
    @Schema(description = "Желаемая дата поездки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime desiredDate;
    
    @Schema(description = "Время согласования заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime approveDate;
    
    @Schema(description = "Флаг совместной поездки")
    private boolean coopTrip;
    
    @Schema(description = "Автомобиль")
    private String car;
    
    @Schema(description = "Время начала аренды")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime rentCreatedTime;
    
    @Schema(description = "Время завершения аренды")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime rentFinishedTime;
    
    @Schema(description = "Дата закрытия обращения")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime finishedTime;
    
    @Schema(description = "Адрес начала аренды")
    private String startAddress;
    
    @Schema(description = "Адрес завершения аренды")
    private String finishAddress;
    
    @Schema(description = "Общее время брони (мин) план")
    private long expectedTime;
    
    @Schema(description = "Общее время брони (мин) факт")
    private Integer reserveTime;
    
    @Schema(description = "Общее время поездки (мин)")
    private Integer drivingTime;
    
    @Schema(description = "Общее время ожидания (мин)")
    private Integer parkingTime;
    
    @Schema(description = "Общий пробег (км), план")
    private Double expectedDistance;
    
    @Schema(description = "Общий пробег (км), факт")
    private Integer drivingLength;
    
    @Schema(description = "Общая стоимость брони (руб), факт")
    private Double reserveTimeCost;
    
    @Schema(description = "Общая стоимость поездки (руб), план")
    private Double expectedCost;
    
    @Schema(description = "Общая стоимость поездки (руб), факт")
    private Double drivingTimeCost;
    
    @Schema(description = "Общая стоимость ожидания (руб)")
    private Double parkingTimeCost;
    
    @Schema(description = "Дополнительная стоимость за пробег (руб)")
    private Double drivingLengthCost;
    
    @Schema(description = "Итоговая стоимость аренды (руб)")
    private Double totalCost;
    
    @Schema(description = "Данные подразделения")
    private DepartmentShortDTO department;
    
    @Schema(description = "Идентификатор используемого тарифа")
    private TariffShortDTO tariff;
    
    @Schema(description = "Подразделение 1 уровня")
    String passengerDepartment1;
    
    @Schema(description = "Подразделение 2 уровня")
    String passengerDepartment2;
    
    @Schema(description = "Подразделение 3 уровня")
    String passengerDepartment3;
    
    @Schema(description = "Подразделение 4 уровня")
    String passengerDepartment4;
    
    @Schema(description = "Подразделение 5 уровня")
    String passengerDepartment5;
    
    @Schema(description = "Подразделение 6 уровня")
    String passengerDepartment6;
    
    @Schema(description = "Адрес отправления")
    private String departureAddress;
    
    @Schema(description = "Промежуточные адреса")
    private String intermediateAddresses;
    
    @Schema(description = "Адрес назначения")
    private String destinationAddress;
    
    @Schema(description = "Количество пассажиров")
    private Integer passengerCount;
    
    @Schema(description = "Оценка поездки")
    private Integer ratingMark;
    
    @Schema(description = "Комментарий к оценке поездки")
    private String ratingComment;
    
    @Schema(description = "Список присоединённых пассажиров")
    private String joinedPassengers;
    
    @Schema(description = "Источник")
    private String source;
    
    @Schema(description = "Фиксированная стоимость минимально расчетного тарифа по такси")
    private Double minTaxiTariffCost;
    
    @Schema(description = "Комментарий к цели")
    private String commentForPurpose;
    
    
    @Schema(description = "Код причины отмены")
    private Integer requestStatusCode;
    
    @Schema(description = "Экономия")
    private Boolean savings;
    
    @Schema(description = "Нарушение КС (да/нет)")
    private String deadlineViolation;
    
    @Schema(description = "Номер договора")
    private String contractNumber;
    
    @Schema(description = "Cумма в списания лимита")
    private Double limitDebit;
    
    @Schema(description = "Экономия бюджета для подразделения ")
    private Double departmentEconomy;
    
    @Schema(description = "Итоговый финансовый эффект для организации")
    private Double financialImpact;
    
    @Schema(description = "Итоговый финансовый эффект для организации (попутчики)")
    private Double financialImpactJoined;
    
    @Schema(description = "Дата закрытия обращения")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime requestClosedDatetime;


    @Schema(description = "ID группы исполнителей")
    private UUID executorGroupId;

    @Schema(description = "Наименование группы исполнителей")
    private String executorGroupName;
}
