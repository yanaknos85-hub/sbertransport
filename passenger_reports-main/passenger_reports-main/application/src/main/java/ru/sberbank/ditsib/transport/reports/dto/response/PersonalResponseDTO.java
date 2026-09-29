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
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.*;
import ru.sberbank.ditsib.transport.reports.dto.personalTransport.PersonalCarDTO;
import ru.sberbank.ditsib.transport.reports.dto.personalTransport.SharedRideKpiDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonPropertyOrder({ "id" })
@Schema(title = "Информация", description = "Информация о заказе личного транспорта")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PersonalResponseDTO implements ReportResponseDTO {
    @Schema(description = "Идентификатор")
    private UUID id;
    
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
    
    @Schema(description = "Идентификатор лимита (человекочитаемый) ")
    private String humanReadableLimitId;
    
    @Schema(description = "Автор")
    private EmployeeDTO author;
    
    @Schema(description = "Пассажир")
    private EmployeeDTO passenger;
    
    @Schema(description = "Все Пассажиры")
    private List<EmployeeDTO> passengers;
    
    @Schema(description = "Характер деятельности сотрудника")
    private ItinerantType itinerantType;
    
    @Schema(description = "Транспортное средство")
    private PersonalCarDTO personalCar;
    
    @Schema(description = "Место возникновения затрат")
    private String costCenter;
    
    @Schema(description = "Данные подразделения")
    private DepartmentShortDTO department;
    
    @Schema(description = "Данные должности сотрудника")
    private PositionShortDTO position;
    
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
    
    @Schema(description = "Время согласования заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime orderPaymentFormationStartDate;
    
    @Schema(description = "Тип транспорта")
    private TransportTypeEnum transportType;
    
    @Schema(description = "Статус заявки")
    private TripRequestStatus status;
    
    @Schema(description = "Код статуса")
    private Integer statusCode;
    
    @Schema(description = "Цель поездки")
    private TripPurposeDTO purpose;
    
    @Schema(description = "Флаг совместной поездки")
    private boolean coopTrip;
    
    @Schema(description = "Идентификатор совместной поездки (Только для совместных поездок)")
    private UUID sharedRideId;
    
    @Schema(description = "Количество пассажиров")
    @Builder.Default
    private int passengerCount = 1;
    
    @Schema(description = "Расчетные данные по маршруту")
    private ExpectedDataDTO expected;
    
    @Schema(description = "Фактические данные по маршруту")
    private FactDataDTO factData;
    
    @Schema(description = "Данные по фактической оплате")
    private List<PaymentDataDTO> paymentDataList;
    
    @Schema(description = "Период выплаты")
    private Integer paymentPeriod;
    
    @Schema(description = "Расчетный KPI для совместной поездки")
    private SharedRideKpiDTO kpi;
    
    @Schema(description = "Оценка заявки")
    private RequestRatingDTO requestRating;
    
    @Schema(description = "Дата закрытия заявки")
    private LocalDateTime paymentTime;
    
    @Schema(description = "Рассчитанный коэффициент части оплаты поездки для каждого заказа поездки")
    private Double costSharePart;
    
    @Schema(description = "Экономия для текущего заказа, копеек")
    private Long savingsCash;
    
    @Schema(description = "Экономия в процентах для текущего заказа")
    private Long savingsProcents;
    
    @Schema(description = "ФИО водителя совместной поездки")
    private String driverFIO;
    
    @Schema(description = "Инициатор поездки")
    private Boolean sharedRideOwner;
    
    @Schema(description = "Сумма доплаты за всех пассажиров, копеек")
    private Long additionalSum;
    
    @Schema(description = "Количество присоединившихся пассажиров (заявок)")
    private Integer numberPassengersJoined;
    
    @Schema(description = "Временная зона")
    private String timeZone;
    
    @Schema(description = "Номер тарифа")
    private String tariff;
    
    @Schema(description = "Сумма к выплате, копеек")
    private Long paymentCost;
    
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
    String departureAddress;
    
    @Schema(description = "Промежуточные адреса")
    String intermediateAddresses;
    
    @Schema(description = "Адрес назначения")
    String destinationAddress;
    
    @Schema(description = "Список присоединённых пассажиров")
    String joinedPassengers;
    
    @Schema(description = "Источник")
    private String source;
    
    @Schema(description = "Фиксированная стоимость минимально расчетного тарифа по такси")
    private Double minTaxiTariffCost;
    
    @Schema(description = "Количество присоединенных заявок")
    private Integer totalSharedRequestCount;
    
    @Schema(description = "Cумма в списания лимита")
    private Double limitDebit;
    
    @Schema(description = "Экономия бюджета для подразделения ")
    private Double departmentEconomy;
    
    @Schema(description = "Итоговый финансовый эффект для организации")
    private Double financialImpact;
    
    @Schema(description = "Итоговый финансовый эффект для организации (совместные поездки)")
    private Double financialImpactShared;
    
    @Schema(description = "Итоговый финансовый эффект для организации (попутчики)")
    private Double financialImpactJoined;
    
    @Schema(description = "Комментарий к цели")
    private String commentForPurpose;
    
    @Schema(description = "Код причины отмены")
    private Integer requestStatusCode;
    
    @Schema(description = "Экономия")
    private Boolean savings;
    
    @Schema(description = "Нарушение КС (да/нет)")
    private String deadlineViolation;
    
    @Schema(description = "Контрольный срок")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime deadline;
    
    @Schema(description = "Дата закрытия обращения")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime requestClosedDatetime;

    @Schema(description = "ID группы исполнителей")
    private UUID executorGroupId;

    @Schema(description = "Наименование группы исполнителей")
    private String executorGroupName;

    @Schema(description = "Дата и время начала поездки")
    private LocalDateTime tripStartTime;
}
