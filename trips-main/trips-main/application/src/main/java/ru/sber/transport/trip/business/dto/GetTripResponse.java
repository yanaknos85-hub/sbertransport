package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.trip.serializer.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetTripResponse (

    @Schema(description = "Признак успешности операции")
    boolean isSuccess,

    @Schema(description = "Данные заказа")
    Order order,

    @Schema(description = "Данные заказа")
    Error error

){
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Order {

        /**
         * Идентификатор поездки
         */
        private String orderPartnerId;

        /**
         * Время создания поездки
         */
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime createOrderTime;

        /**
         * Время начала поездки
         */
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime collectionTime;

        /**
         * Стоимость поездки
         */
        private Long price;

        /**
         * Дистанция поездки
         */
        private Double distance;

        /**
         * Код статуса поездки
         */
        private Integer statusCode;

        /**
         * Время окончания поездки
         */
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime finishTime;

        /**
         * Время прибытия водителя
         */
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime performerArrivalTime;

        /**
         * Информация о водителе
         */
        private DriverInfo driver;

        /**
         * Время ожидания, мин
         */
        private int waitTime;

        /**
         * Идентификатор поездки на стороне СберТранспорта
         */
        private String orderSbertransportId;

        /**
         * Фактическая длительность поездки, секунды
         */
        private Integer waitTimeOW;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DriverInfo {

        /**
         * Идентификатор
         */
        private UUID id;

        /**
         * Имя
         */
        private String name;

        /**
         * Отчество
         */
        private String patronymic;

        /**
         * Фамилия
         */
        private String secName;

        /**
         * Телефон
         */
        private String phone;

        /**
         * Широта
         */
        private Double latitude;

        /**
         * Долгота
         */
        private Double longitude;

        /**
         * Данные автомобиля
         */
        private VehicleInfo vehicle;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VehicleInfo {

        /**
         * Марка
         */
        private String mark;

        /**
         * Модель
         */
        private String model;

        /**
         * Цвет
         */
        private String color;

        /**
         * Госномер
         */
        private String registrationNumber;
    }

    /**
     * Объект ошибки.
     *
     * @param status  Статус ошибки.
     * @param message Сообщение об ошибке.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(

            @Schema(title = "Статус", description = "Статус ошибки")
            int status,

            @Schema(title = "Сообщение", description = "Сообщение об ошибке")
            String message
    ) {
    }
}