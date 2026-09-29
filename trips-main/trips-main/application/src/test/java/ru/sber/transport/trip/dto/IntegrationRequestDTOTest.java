package ru.sber.transport.trip.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.trip.business.dto.IntegrationRequestDTO;
import ru.sber.transport.trip.serializer.OffsetDateTimeDeserializerTest;
import ru.sber.transport.trip.serializer.OffsetDateTimeSerializerTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сообщение с заявкой.
 */
@Getter
@Setter
@NoArgsConstructor
public class IntegrationRequestDTOTest { //NOSONAR

    /**
     * ID заявки в АС СберТранспорт.
     */
    private UUID requestId;

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
    @JsonDeserialize(using = OffsetDateTimeDeserializerTest.class)
    @JsonSerialize(using = OffsetDateTimeSerializerTest.class)
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
    private IntegrationRequestDTO.Expected expected;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderRoutePoints{

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
    public static class Waypoint{

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
    public static class Contact{

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
    public static class Expected {

        /**
         * Предполагаемая длительность поездки
         */
        private Long time;

        /**
         * Идентификатор планируемого автомобиля
         */
        private UUID vehicleId;
    }
}

