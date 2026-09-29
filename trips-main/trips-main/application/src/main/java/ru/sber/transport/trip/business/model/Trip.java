package ru.sber.transport.trip.business.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Поездка.
 */
@Getter
@Setter
public final class Trip {
    /**
     * Идентификатор поездки.
     */
    private UUID id;

    /**
     * Время начала поездки.
     */
    private OffsetDateTime factStartTime;

    /**
     * Время окончания поездки.
     */
    private OffsetDateTime factEndTime;

    /**
     * Статус поездки.
     */
    private TripStatus status = TripStatus.WAITING_FOR_ASSIGNMENT;

    /**
     * Путевык точки.
     */
    private List<Waypoint> waypoints = new ArrayList<>();

    /**
     * Идентификатор контрагента.
     */
    private UUID contractorId;

    /**
     * Заявки.
     */
    private Set<Request> requests = new HashSet<>();

    /**
     * Порядковый номер поездки.
     */
    private Long digitId;

    /**
     * Фактическая дистанция поездки.
     */
    private Double factDistance;

    /**
     * Идентификатор водителя.
     */
    private UUID driverId;

    /**
     * Идентифмкатор автомобиля.
     */
    private UUID vehicleId;

    /**
     * Количество пассажиров.
     */
    private int passengerCount;

    /**
     * Класс такси.
     */
    private String taxiClass;

    /**
     * Время ожидания водителя.
     */
    private Duration driverWaitingTime;

    /**
     * Идентификатор диспетчера.
     */
    private UUID dispatcherId;

    /**
     * Время подачи автомобиля.
     */
    private OffsetDateTime arrivedDate;

    /**
     * Человекочитаемый идентификатор поездки.
     */
    private String humanReadableId;

    /**
     * Счетчик автоматического назначения.
     */
    private Integer autoassignCounter = 0;

    /**
     * Время создания поездки.
     */
    private OffsetDateTime creationTime;

    /**
     * Планируемая стоимость.
     */
    private Long expectedCost;

    /**
     * Планируемая дистанция.
     */
    private Double expectedDistance;

    /**
     * Планируемое время.
     */
    private Long expectedTime;

    /**
     * Планируемая смена обработки поездки.
     */
    private UUID plannedShiftId;

    /**
     * Временная зона.
     */
    private String timeZone;

    /**
     * Внешний человекочитаемый идентификатор поездки.
     */
    private String externalHumanReadableId;

    /**
     * Комментарий.
     */
    private String comment;

    /**
     * Признак готовности отчета по поездке.
     */
    private boolean reportCreated;

    /**
     *  Объект из ожидаемых данных
     */
    private UUID expectedVehicleId;

    /**
     * Объект данных, который необходим для трансфера
     */
    private Information information;

    /**
     * Ожидаемое время начала поездки
     */
    private OffsetDateTime expectedStartTime;

    /**
     * Ожидаемое время окончания поездки
     */
    private OffsetDateTime expectedEndTime;

    /**
     * Фактическая стоимость в копейках
     */
    private Long factCost;

    /**
     * Идентификатор филиала автопарка
     */
    private UUID autoparkId;

    /**
     * Время взятия заявки в работу диспетчером
     */
    private OffsetDateTime dispatcherTakeToWork;

    /**
     * Время последнего изменения статуса
     */
    private OffsetDateTime statusChangedAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Information {

        /**
         * Признак необходимости в детском кресле
         */
        private Boolean childSeat;

        /**
         * Объект о кол-ве кресел
         */
        private ChildSeatDetails childSeatDetails;

        /**
         * Признак наличия багажа
         */
        private Boolean bugs;

        /**
         * Количество багажа, комментарий
         */
        private String bugsComment;

        /**
         * Признак наличия негабаритного багажа
         */
        private Boolean bugsOversized;

        /**
         * Негабаритный багаж, комментарий
         */
        private String bugsOversizedComment;

        /**
         * Признак наличия животного
         */
        private Boolean animal;

        /**
         * Животные, комментарий
         */
        private String animalComment;

        /**
         * Номер рейса/поезда
         */
        private String numberFlight;

        /**
         * Дата и время рейса/поездка
         */
        private OffsetDateTime dateFlight;

        /**
         * Номер в гостинице
         */
        private String phoneHotel;

        /**
         * Телефон доп. контакта
         */
        private String addContactPhone;

        /**
         * ФИО доп. контакта
         */
        private String addContactFIO;

        /**
         * Вид транспорт
         */
        private String typeVehicle;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChildSeatDetails {

        /**
         * Кресло от 9 мес. до 4 лет : число
         */
        private Integer group1;

        /**
         * Кресло 3-7 лет : число
         */
        private Integer group2;

        /**
         * Бустер 6-12 лет: число
         */
        private Integer booster;

        /**
         * Люлька до 1 года: число
         */
        private Integer newborn;
    }
}
