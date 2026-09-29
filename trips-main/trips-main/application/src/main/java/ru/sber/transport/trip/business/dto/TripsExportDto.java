package ru.sber.transport.trip.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Описание структуры данных импорта/экспорта.
 */
public record TripsExportDto(
        int number,

        @Schema(description = "ID поездки")
        String id,

        @Schema(description = "ID заявки(-ок)")
        @NullRender
        String requestIds,

        @Schema(description = "Дата создания заявки")
        @NullRender
        LocalDate creationDate,

        @Schema(description = "Время создания заявки")
        @NullRender
        LocalTime creationTime,

        @Schema(description = "Желаемая дата начала поездки")
        @NullRender
        LocalDate expectedStartDate,

        @Schema(description = "Желаемое время начала поездки")
        @NullRender
        LocalTime expectedStartTime,

        @Schema(description = "Фактическая дата начала поездки")
        @NullRender
        LocalDate factStartDate,

        @Schema(description = "Фактическое время начала поездки")
        @NullRender
        LocalTime factStartTime,

        @Schema(description = "Дата просмотра заявки диспетчером")
        @NullRender
        LocalDate dispatcherTakeToWorkDate,

        @Schema(description = "Время просмотра заявки диспетчером")
        @NullRender
        LocalTime dispatcherTakeToWorkTime,

        @Schema(description = "Дата изменения статуса заявки диспетчером")
        @NullRender
        LocalDate statusChangedDate,

        @Schema(description = "Время изменения статуса заявки диспетчером")
        @NullRender
        LocalTime statusChangedTime,

        @Schema(description = "Адрес подачи")
        @NullRender
        String routeStart,

        @Schema(description = "Промежуточные адреса")
        @NullRender
        String routePoints,

        @Schema(description = "Адрес завершения")
        @NullRender
        String routeEnd,

        @Schema(description = "Плановое количество точек маршрута")
        @NullRender
        Integer checkinQuantity,

        @Schema(description = "Хронология обработки статусов Водителем в приложении")
        @NullRender
        String checkinStatus,

        @Schema(description = "Проверка геопозиции при прохождении точек маршрута водителем. Да - был на точке. Нет - не было на точке.")
        @NullRender
        String checkinConfirmation,

        @Schema(description = "Статус поездки")
        @NullRender
        String status,

        @Schema(description = "Комментарии для водителя")
        @NullRender
        String comment,

        @Schema(description = "Пассажир поездки")
        @NullRender
        String passenger,

        @Schema(description = "Номер автомобиля")
        @NullRender
        String vehicleNumber,

        @Schema(description = "Водитель")
        @NullRender
        String driver,

        @Schema(description = "Плановая протяженность (км)")
        @NullRender
        Double expectedDistance,

        @Schema(description = "Плановое время поездки (мин)")
        @NullRender
        Long expectedTime,

        @Schema(description = "Плановое ожидание на точках (мин)")
        @NullRender
        Long expectedWaitTime,

        @Schema(description = "Плановая стоимость (руб)")
        @NullRender
        Double expectedCost,

        @Schema(description = "Фактическая протяженность (км)")
        @NullRender
        Double factDistance,

        @Schema(description = "Фактическое время поездки (мин)")
        @NullRender
        Long factTime,

        @Schema(description = "Фактическое время ожидания (мин)")
        @NullRender
        Long factWaitTime,

        @Schema(description = "Тип поездки")
        @NullRender
        String tripType,

        @Schema(description = "Диспетчер")
        @NullRender
        String dispatcherName,

        @Schema(description = "Телефон диспетчера")
        @NullRender
        String dispatcherPhone,

        @Schema(description = "Класс авто")
        @NullRender
        String taxiClass,

        @Schema(description = "Кол-во пассажиров")
        int passengerCount
) {

    public record Checkin(@NullRender Integer quantity, @NullRender String status, @NullRender String waypointConfirmation) {}

}