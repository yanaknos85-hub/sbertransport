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
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.reports.dto.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonPropertyOrder({ "id" })
@Schema(title = "Информация", description = "Информация о заказе такси")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaxiResponseDTO implements ReportResponseDTO {
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
    private List<EmployeeDTO> passengers = new ArrayList<>();
    
    @Schema(description = "Характер деятельности сотрудника")
    private ItinerantType itinerantType;
    
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
    
    @Schema(description = "Тип транспорта")
    private TransportTypeEnum transportType;
    
    @Schema(description = "Класс такси")
    private TaxiClass taxiClass;
    
    @Schema(description = "Статус согласования")
    private ApprovalState approvalState;
    
    @Schema(description = "Согласующий")
    private EmployeeDTO approvedBy;
    
    @Schema(description = "Идентификатор используемого тарифа")
    private TariffShortDTO tariff;
    
    @Schema(description = "Статус заявки")
    private TripRequestStatus status;
    
    @Schema(description = "Код статуса")
    private Integer statusCode;
    
    @Schema(description = "Цель поездки")
    private TripPurposeDTO purpose;
    
    @Schema(description = "Флаг совместной поездки")
    private boolean coopTrip;
    
    @Schema(description = "Информация о контрагенте")
    private ContractorDTO contractor;
    
    @Schema(description = "Идентификатор совместной поездки (Только для совместных поездок)")
    private UUID sharedRideId;
    
    @Schema(description = "Экономия для текущего заказчика, в руб")
    private Double kpiSavings;
    
    @Schema(description = "Количество пассажиров")
    @Builder.Default
    private int passengerCount = 1;
    
    @Schema(description = "Комментарий для водителя")
    private String commentForDriver;
    
    @Schema(description = "Оценка заявки")
    private RequestRatingDTO requestRating;
    
    @Schema(description = "Расчетные данные по маршруту")
    private ExpectedDataDTO expected;
    
    @Schema(description = "Фактические данные по маршруту")
    private FactDataDTO factData;
    
    /**
     * Дата внесения фактических параметров поездки
     */
    @Schema(description = "Дата внесения фактических параметров поездки")
    private LocalDateTime factParametersSettingTime;
    
    @Schema(description = "Водитель")
    private DriverDTO driver;
    
    @Schema(description = "Автомобиль")
    private VehicleDTO vehicle;
    
    @Schema(description = "ID поездки")
    private UUID tripId;
    
    @Schema(description = "Человекочитаемый ID поездки")
    private String tripHumanReadableID;
    
    @Schema(description = "Дата закрытия обращения")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime finishedTime;
    
    @Schema(description = "Доля в поездке")
    private Double costSharePart;
    
    @Schema(description = "Экономия в рублях для текущего заказа")
    private Long savingsCash;
    
    @Schema(description = "Экономия в процентах для текущего заказа")
    private Long savingsProcents;
    
    @Schema(description = "Инициатор поездки")
    private Boolean sharedRideOwner;
    
    @Schema(description = "Временная зона")
    private String timeZone;
    
    @Schema(description = "Подразделение 1 уровня")
    private String passengerDepartment1;
    
    @Schema(description = "Подразделение 2 уровня")
    private String passengerDepartment2;
    
    @Schema(description = "Подразделение 3 уровня")
    private String passengerDepartment3;
    
    @Schema(description = "Подразделение 4 уровня")
    private String passengerDepartment4;
    
    @Schema(description = "Подразделение 5 уровня")
    private String passengerDepartment5;
    
    @Schema(description = "Подразделение 6 уровня")
    private String passengerDepartment6;
    
    @Schema(description = "Адрес отправления")
    private String departureAddress;
    
    @Schema(description = "Промежуточные адреса")
    private String intermediateAddresses;
    
    @Schema(description = "Адрес назначения")
    private String destinationAddress;
    
    @Schema(description = "Резолюция")
    private String resolution;
    
    @Schema(description = "Организация")
    private UUID organizationId;
    
    @Schema(description = "Лимит")
    private LimitDTO limit;
    
    @Schema(description = "Контрольный срок подачи ТС")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime deadline;
    
    @Schema(description = "Фактическое время подачи ТС")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime driverArrivedDatetime;
    
    @Schema(description = "Нарушение КС (да/нет)")
    private String deadlineViolation;
    
    @Schema(description = "Наименование организации")
    private String organizationOfficialName;
    
    @Schema(description = "Количество присоединившихся пассажиров (заявок)")
    private Integer numberPassengersJoined;
    
    @Schema(description = "Время согласования заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime approveDate;
    
    @Schema(description = "Время закрытия заявки АС")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime requestClosedDatetime;
    
    @Schema(description = "Список присоединённых пассажиров")
    private String joinedPassengers;
    
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
    
    @Schema(description = "Номер договора")
    private String contractNumber;
    
    @Schema(description = "Вид тарифа")
    private String tripClass;
    
    @Schema(description = "ID группы исполнителей")
    private UUID executorGroupId;
    
    @Schema(description = "Наименование группы исполнителей")
    private String executorGroupName;
    
    @Schema(description = "Время ожидания в реестре контрагента, мин.й")
    private Double registryFactWaitingTime;
    
    @Schema(description = "Дистанция в реестре контрагента, км")
    private Double registryFactDistance;
    
    @Schema(description = "Стоимость в реестре контрагента , руб")
    private Double registryFactCost;
    
    @Schema(description = "Номер реестра оплаты")
    private String registryHumanReadableId;
    
    @Schema(description = "Статус оплаты")
    private String registryFactPayment;
}
