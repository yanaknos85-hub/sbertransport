package ru.sber.transport.request.external.messaging.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.messaging.Message;

@Value
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestMessage implements Message<UUID> {

    UUID id;
    String tariff;
    UUID passengerId;
    String humanReadableId;
    List<WaypointMessage> waypoints;
    UUID purposeId;
    UUID organizationId;
    UUID departmentId;
    UUID approverId;

    Instant approvalDate;
    Instant date;
    String timeZone;
    String status;
    String comment;
    String reason;
    ActualData actual;
    PlannedData planned;
    List<Assessment> assessments;

    @Value
    @Builder
    public static class WaypointMessage {
        /**
         * Идентификатор путевой точки
         */
        UUID id;
        /**
         * Название страны
         */
        String country;
        /**
         * Название региона
         */
        String region;
        /**
         * Название города
         */
        String city;
        /**
         * Название улицы
         */
        String street;
        /**
         * Номер дома
         */
        String house;
        /**
         * Корпус
         */
        String building;
        /**
         * Строение
         */
        String structure;
        /**
         * Долгота
         */
        double longitude;
        /**
         * Широта
         */
        double latitude;
    }

    @Value
    @Builder
    public static class PlannedData {
        /**
         * Расстояние
         */
        long distance;
        /**
         * Длительность
         */
        String duration;
        /**
         * Стоимость
         */
        double cost;
    }

    @Value
    @Builder
    public static class ActualData {
        Double cost;
    }

    @Value
    @Builder
    public static class Assessment {
        /** Тип оценки */
        String type;
        /** Рейтинг */
        int rating;
        /** Комментарий */
        String comment;
    }
}


