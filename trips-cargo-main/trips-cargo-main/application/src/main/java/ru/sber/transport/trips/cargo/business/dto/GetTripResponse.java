package ru.sber.transport.trips.cargo.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.serializer.OffsetDateTimeSerializer;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Объект ответа на запрос получения информации по поездке.
 *
 * @param isSuccess признак успешности операции.
 * @param order данные о поездке.
 * @param error информация об ошибке в рамках выполнения запроса.
 */
@Schema(title = "Поездка", description = "Данные поездки")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetTripResponse(

        boolean isSuccess,

        Order order,

        Error error
) {

    /**
     * Объект поездки.
     *
     * @param orderParthnerId идентификатор.
     * @param orderSbertransportId идентификатор на стороне СберТранспорта.
     * @param createOrderTime дата создания заказа.
     * @param collectionTime дата начала доставки.
     * @param price стоимость заказа, коп
     * @param distance преодолённая дистанция, км
     * @param statusCode код статуса заказа
     * @param requests заявки в заказе
     * @param finishTime дата закрытия заказа
     * @param performerArrivalTime время прибытия водителя
     * @param driver водитель
     * @param waitTime время ожидания, мин
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Order(
            @Schema(title = "Идентификатор", description = "Идентификатор")
            String orderParthnerId,

            @Schema(title = "Идентификатор", description = "Идентификатор на стороне СберТранспорта")
            String orderSbertransportId,

            @Schema(title = "Дата", description = "Дата создания заказа")
            @JsonSerialize(using = OffsetDateTimeSerializer.class)
            OffsetDateTime createOrderTime,

            @Schema(title = "Дата", description = "Дата начала доставки")
            @JsonSerialize(using = OffsetDateTimeSerializer.class)
            OffsetDateTime collectionTime,

            @Schema(title = "Стоимость", description = "Стоимость заказа, коп")
            Long price,

            @Schema(title = "Дистанция", description = "Преодолённая дистанция, км")
            Double distance,

            @Schema(title = "Код", description = "Код статуса заказа")
            int statusCode,

            @Schema(title = "Заявки", description = "Список заявок в заказе")
            List<Request> requests,

            @Schema(title = "Дата", description = "Дата закрытия заказа")
            @JsonSerialize(using = OffsetDateTimeSerializer.class)
            OffsetDateTime finishTime,

            @Schema(title = "Дата", description = "Дата прибытия водителя")
            @JsonSerialize(using = OffsetDateTimeSerializer.class)
            OffsetDateTime performerArrivalTime,

            @Schema(title = "Водитель", description = "Данные водителя")
            Driver driver,

            @Schema(title = "Время", description = "Время ожидания, мин")
            Long waitTime,

            @Schema(title = "Время", description = "Время работы грузчиков, мин")
            Long loadersWorkTime,

            @Schema(title = "Количество", description = "Фактическое количество грузчиков, чел")
            Integer factLoaders
    ){}

    /**
     * Данные водителя.
     *
     * @param secName     фамилия.
     * @param name    имя.
     * @param patronymic   отчество.
     * @param phone контактный номер.
     * @param vehicle автомобиль.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Driver(

            @Schema(title = "Фамилия", description = "Фамилия водителя")
            String secName,

            @Schema(title = "Имя", description = "Имя водителя")
            String name,

            @Schema(title = "Отчество", description = "Отчество водителя")
            String patronymic,

            @Schema(title = "Контактный номер", description = "Контактный номер водителя")
            String phone,

            @Schema(title = "Автомобиль", description = "Данные автомобиля")
            Vehicle vehicle

    ) {
    }

    /**
     * Фактические данные.
     *
     * @param mark       бренд.
     * @param model       модель.
     * @param registrationNumber регистрационный номер.
     * @param color       цвет.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Vehicle(

            @Schema(title = "Бренд", description = "Бренд автомобиля")
            String mark,

            @Schema(title = "Модель", description = "Модель автомобиля")
            String model,

            @Schema(title = "Регистрационный номер", description = "Регистрационный номер автомобиля")
            String registrationNumber,

            @Schema(title = "Цвет", description = "Цвет автомобиля")
            String color

    ) {
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

    /**
     * Данные по заявке.
     *
     * @param humanReadableId человекочитаемый идентификатор заявки.
     * @param qrs список qr-кодов.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Request(

            @Schema(title = "HRID", description = "Человекочитаемый идентификатор заявки")
            String humanReadableId,

            @Schema(title = "QR", description = "Список qr-кодов")
            List<String> qrs
    ) {
    }
}
