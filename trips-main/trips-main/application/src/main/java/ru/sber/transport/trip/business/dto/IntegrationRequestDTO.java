package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import ru.sber.transport.trip.serializer.OffsetDateTimeDeserializer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сообщение с заявкой.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrationRequestDTO {

    /**
     * ID заявки в АС СберТранспорт.
     */
    private UUID requestId;

    /**
     * ID подразделения.
     */
    private UUID departmentId;

    /**
     * Код статуса.
     */
    private String statusCode;

    /**
     * Идентификатор тарифа.
     */
    private int tariff;

    /**
     * Плановое время начала поездки.
     */
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime planStartTime;

    /**
     * Tочки маршрута.
     */
    private OrderRoutePoints routePoints;

    /**
     * Временная зона.
     */
    @JsonAlias(value = "class")
    private String tripClass;

    /**
     * Тип транспорта заявки.
     */
    private String inn;

    /**
     * Класс поездки.
     */
    private String workGroup;

    /**
     * Промежуточные точки поездки.
     */
    private String comment;

    /**
     * Человекочитаемый идентификатор поездки на строне СберТранспорта
     */
    private String humanReadableId;

    /**
     *  Объект из ожидаемых данных
     */
    private Expected expected;

    /**
     * Объект данных, который необходим для трансфера
     */
    private Information information;

    /**
     *  Количество пассажиров
     */
    private int passengerCount;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderRoutePoints {

        /**
         * Точка старта
         */
        Waypoint source;

        /**
         * Финальная точка
         */
        Waypoint destination;

        /**
         * Промежуточные точки маршрута
         */
        List<Waypoint> waypoints;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Waypoint {

        /**
         * Строковая репрезентация адреса
         */
        private String name;

        /**
         * Широта
         */
        private Double latitude;

        /**
         * Долгота
         */
        private Double longitude;

        /**
         * Время ожидания на точке (секунды)
         */
        private Long waitTime;

        /**
         * Пассажиры
         */
        private List<Contact> passengers;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {

        /**
         * Действие пассажира на точке
         */
        private String type;

        /**
         * Имя пассажира
         */
        private String firstName;

        /**
         * Отчество пассажира
         */
        private String patronymic;

        /**
         * Номер телефона пассажира
         */
        private String mobilePhone;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Expected {

        /**
         * Предполагаемая длительность поездки
         */
        private Long time;

        /**
         * Предполагаемая дистанция поездки
         */
        private Double distance;

        /**
         * Идентификатор планируемого автомобиля
         */
        private UUID vehicleId;
    }

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
    @JsonIgnoreProperties(ignoreUnknown = true)
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
