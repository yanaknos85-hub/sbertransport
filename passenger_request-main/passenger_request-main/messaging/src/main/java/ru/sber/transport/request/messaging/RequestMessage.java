package ru.sber.transport.request.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.messaging.Message;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Сообщение с заявкой.
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestMessage implements Message<UUID> {

    /**
     * Идентификатор заявки.
     */
    private UUID id;

    /**
     * Человекочитаемый идентификатор заявки.
     */
    private String humanReadableId;

    /**
     * Идентификатор автора.
     */
    private UUID authorId;

    /**
     * Автор.
     */
    private Employee author;

    /**
     * Идентификатор пассажира.
     */
    private UUID passengerId;

    /**
     * Пассажир.
     */
    private Employee passenger;

    /**
     * Идентификатор организации.
     */
    private UUID organizationId;

    /**
     * Резолюция.
     */
    private String resolution;

    /**
     * Дата создания заявки.
     */
    private LocalDateTime creationTime;

    /**
     * Временная зона.
     */
    private String timeZone;

    /**
     * Дата окончания заявки.
     */
    private LocalDateTime finishedTime;

    /**
     * Тип транспорта заявки.
     */
    private String transportType;

    /**
     * Класс поездки.
     */
    private String tripClass;

    /**
     * Промежуточные точки поездки.
     */
    private List<Waypoint> waypoints;

    /**
     * Идентификатор одобрения.
     */
    private UUID approvalId;

    /**
     * Идентификатор тарифа.
     */
    private UUID tariffId;

    /**
     * Идентификатор тарифа.
     */
    private UUID outcomeTariffId;

    /**
     * JSON тариф.
     */
    private Map<String, Object> tariff;

    /**
     * JSON тариф.
     */
    private Map<String, Object> outcomeTariff;

    /**
     * Дата одобрения.
     */
    private LocalDateTime approvalDate;

    /**
     * Флаг совместной поездки.
     */
    private boolean coopTrip;

    /**
     * Человекочитаемый идентификатор трипа.
     */
    private String taxiTripHrId;

    /**
     * Ожидаемая дата.
     */
    private LocalDateTime desiredDate;

    /**
     * Дата подтверждения поездки.
     */
    private LocalDateTime tripConfirmationDate;

    /**
     * Флаг включения sla.
     */
    private Boolean isSlaExpired;

    /**
     * Дата старта формирования оплаты.
     */
    private LocalDateTime orderPaymentFormationStartDate;

    /**
     * Дата окончания формирования оплаты.
     */
    private LocalDateTime orderPaymentFormationFinishingDate;

    /**
     * Состояние дедлайна.
     */
    private String deadlineState;

    /**
     * Флаг поездки по пригороду.
     */
    private boolean isSuburbTrip;

    /**
     * Статус заявки
     */
    private String status;

    /**
     * Статус для сервиса метрикс
     */
    private String metricsStatus;

    /**
     * Код подстатуса заявки
     */
    private Integer statusCode;

    /**
     * Количество пассажиров.
     */
    private int passengerCount;

    /**
     * Состояние одобрения.
     */
    private String approvalState;

    /**
     * Ожидаемые данные.
     */
    private ExpectedData expected;

    /**
     * Идентификатор цели.
     */
    private UUID purposeId;

    /**
     * Комментарий для водителя.
     */
    private String commentForDriver;

    /**
     * Флаг владения совместной поездкой.
     */
    private boolean sharedRideOwner;

    /**
     * Идентификатор поездки.
     */
    private UUID rideId;

    /**
     * Флаг удаления заявки.
     */
    private boolean deleted;

    /**
     * Список компенсаций оплаты общественного транспорта.
     */
    @Builder.Default
    private List<TransportCompensation> transportCompensation = new ArrayList<>();

    /**
     * Флаг существования документа компенсации общественного транспорта.
     */
    private boolean publicCompensationDocumentExist;

    /**
     * Идентификатор контрагента.
     */
    private UUID contractorId;

    /**
     * Идентификатор личного авто.
     */
    private UUID personalCarId;

    /**
     * Класс каршеринга.
     */
    private String carsharingClass;

    /**
     * Контрольный срок
     */
    private LocalDateTime deadline;

    /**
     * Дата и время старта поиска водителя на такси
     */
    private LocalDateTime taxiAwaitingSearchStartDate;

    /**
     * ID водителя такси
     */
    private UUID driverId;

    /**
     * Идентификатор автопарка.
     */
    private UUID autoparkId;

    /**
     * Идентификатор транспортного средства.
     */
    private UUID vehicleId;

    /**
     * Данные транспортного средства.
     */
    private VehicleData vehicleData;

    /**
     * Данные водителя.
     */
    private DriverData driverData;

    /**
     * ID водителя личного транспорта (является обычным Employee)
     */
    private UUID employeeDriverId;

    /**
     * Длительность поездки
     */
    private Duration tripFactDuration;

    /**
     * Дистанция поездки
     */
    private Double factDistance;

    /**
     * Данные экономии из совместной поездки
     */
    private EconomyData economyData;

    /**
     * Время ожидания пассажира водителем
     */
    private Duration driverWaitingTime;

    /**
     * Количество присоединившихся пассажиров (заявок)
     */
    private Integer numberPassengersJoined;

    /**
     * Дополнительная сумма для начисления в копейках
     */
    private Long additionalSum;

    /**
     * Номер аренды (каршеринг)
     */
    private Integer rentId;

    /**
     * Номер телефона, который использовался для создания заявки на каршеринг
     */
    private String phoneNumber;

    /**
     * Время перехода заявки в статус Водитель ожидает в точке отправления TAXI_DRIVER_ARRIVED
     */
    private LocalDateTime driverArrivedDatetime;

    /**
     * Время закрытия заявки АС
     */
    private LocalDateTime requestClosedDatetime;

    /**
     * признак вип тарифа
     */
    private boolean vip;

    private String groupTransferClass;

    private NewRequestInformationDTO information;

    private String source;

    private Long minTaxiTariffCost;

    /**
     * Список присоединённых пассажиров
     */
    private Set<UUID> joinedPassengerIds;

    private String commentForPurpose;


    /**
     * идентификатор группы исполнителей
     */
    private UUID executorGroupId;

    /**
     * наименование группы исполнителей
     */
    private String executorGroupName;

    /**
     * Дата и время начала поездки
     */
    private LocalDateTime tripStartTime;

    /**
     * Данные о подозрении на фрод
     */
    private List<Fraud> fraudData;


    /**
     *Фактические данные о поездке
     */
    private FactData factData;


    /**
     *Фактические данные о поездке
     * @param factCost - стоимость поездки
     * @param factDistance - расстояние поездки
     * @param factWaitTime - время ожидания
     * @param factStartTime - время начала поездки
     * @param factFinishTime - время окончания поездки
     * @param factDriverArrivedTime - время прибытия водителя
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FactData(
            Integer factCost,
            Double factDistance,
            Long factWaitTime,
            LocalDateTime factStartTime,
            LocalDateTime factFinishTime,
            LocalDateTime factDriverArrivedTime
    ) {}

    /**
     * Данные о фроде
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fraud(
            String type,
            String comment,
            UUID requestId
    ) {
    }

    /**
     * Компенсация оплаты общественного транспорта.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransportCompensation {

        /**
         * Идентификатор компенсации.
         */
        private UUID id;

        /**
         * Тип компенсации.
         */
        private String compensationType;

        /**
         * Тип транспорта.
         */
        private String transportType;

        /**
         * Стоимость билета (коп).
         */
        private Integer ticketsCost;

        /**
         * Количество билетов.
         */
        @Builder.Default
        private Integer ticketsCount = 1;

        /**
         * Дата начала протухания билета.
         */
        private LocalDate ticketsExpirationStart;

        /**
         * Дата окончания истечения билета.
         */
        private LocalDate ticketsExpirationEnd;

        /**
         * Идентификатор истечения документа.
         */
        private UUID attachedDocumentId;
    }

    /**
     * Промежуточная точка.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Waypoint {

        /**
         * Идентификатор промежуточной точки.
         */
        private UUID id;

        /**
         * Сообщение адреса промежуточной точки.
         */
        private AddressMessage address;

        /**
         * Время ожидания на промежуточной точке.
         */
        private Duration waitTime;

        /**
         * Флаг автоматической проверки.
         */
        private Boolean checkinAutomatic;

        /**
         * Флаг ручной проверки.
         */
        private Boolean checkinManual;

        /**
         * Причина отсутствия.
         */
        private String absenceReason;

        /**
         * Индекс сортировки.
         */
        private Integer orderingIndex;

    }

    /**
     * Ожидаемые данные.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExpectedData {

        /**
         * Стоимость
         */
        private double cost;

        /**
         * Стоимость
         */
        private double outcomeCost;

        /**
         * Расстояние
         */
        private double distance;

        /**
         * Ожидаемое время.
         */
        private Duration time;
    }

    /**
     * Сотрудник.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Employee {

        /**
         * Имя сотрудника.
         */
        private String firstName;

        /**
         * Фамилия сотрудника.
         */
        private String lastName;

        /**
         * Отчество сотрудника.
         */
        private String patronymic;

        /**
         * Личный номер сотрудника.
         */
        private String personnelNumber;

        /**
         * Идентификатор департамента.
         */
        private UUID departmentId;

        /**
         * Идентификатор пользователя.
         */
        private UUID userId;

        /**
         * Идентификатор позиции.
         */
        private UUID positionId;

        /**
         * Идентификатор делегировавшего сотрудника.
         */
        private UUID delegatedById;

        /**
         * Название должности.
         */
        private String positionName;

        /**
         * Идентификатор руководителя.
         */
        private UUID supervisorId;

        /**
         * Идентификатор организации.
         */
        private UUID organizationId;

        /**
         * Название департамента.
         */
        private String departmentName;

        /**
         * Код мвз.
         */
        private String mvz;

        /**
         * Человекочитаемый идентификатор сотрудника.
         */
        private String humanReadableId;

        /**
         * Номер телефона.
         */
        private String mobilePhone;
    }

    /**
     * Данные транспортного средства.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VehicleData {

        /**
         * Брэнд ТС.
         */
        private String brand;

        /**
         * Модель ТС.
         */
        private String model;

        /**
         * Гос номер ТС.
         */
        private String stateNumber;

        /**
         * Цвет ТС.
         */
        private String color;
    }

    /**
     * Модель данных водителя.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DriverData {

        /**
         * Фамилия.
         */
        private String lastName;

        /**
         * Имя.
         */
        private String firstName;

        /**
         * Отчество.
         */
        private String patronymic;

        /**
         * Номер телефона.
         */
        private String phoneNumber;
    }

    /**
     * Модель данных водителя.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EconomyData {

        /**
         * рассчитанный коэффициент части оплаты поездки для каждого заказа поездки.
         */
        private Double costSharePart;

        /**
         * Экономия в рублях для текущего заказа.
         */
        private Long savingsCash;

        /**
         * Экономия в процентах для текущего заказа.
         */
        private Long savingsProcents;

        /**
         * Инициатор поездки.
         */
        private Boolean sharedRideOwner;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewRequestInformationDTO {

        @Schema(description = "Детское кресло")
        private boolean childSeat;

        @Schema(description = "Детское кресло")
        @Builder.Default
        private ChildSeatDetails childSeatDetails = new ChildSeatDetails(0, 0, 0,0);

        @Schema(description = "Количество багажа")
        private boolean bugs;

        @Schema(description = "Количество багажа, комментарий")
        private String bugsComment;

        @Schema(description = "Негабаритный багаж")
        private boolean bugsOversized;

        @Schema(description = "Негабаритный багаж, комментарий")
        private String bugsOversizedComment;

        @Schema(description = "Животные")
        private boolean animal;

        @Schema(description = "Животные, комментарий")
        private String animalComment;

        @Schema(description = "Дополнительное контактное лицо: ФИО +телефон")
        private String addContact;

        @Schema(description = "Дополнительное контактное лицо: Телефон")
        private String addContactPhone ;

        @Schema(description = "Дополнительное контактное лицо: ФИО")
        private String addContactFIO ;

        @Schema(description = "Номер рейса/поезда:  текст")
        private String numberFlight;

        @Schema(description = "Дата и время рейса/поездка")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        private LocalDateTime dateFlight;

        @Schema(description = "Телефон принимающей гостиницы")
        private String phoneHotel;

        @Schema(description = "Желаемый тип Транспортного средства")
        private String typeVehicle;
    }
}
