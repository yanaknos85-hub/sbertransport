package ru.sber.transport.trips.cargo.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.serializer.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Объект поездки.
 *
 * @param id идентификатор.
 * @param humanReadableId человекочитаемый идентификатор.
 * @param status статус.
 * @param requests список заявок.
 * @param waypoints список точек.
 * @param startTime время начала поездки.
 * @param endTime время окончания поездки.
 * @param driver водитель.
 * @param dispatcher диспетчер.
 */
@Schema(title = "Поездка",
        description = "Данные поездки")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TripV2Dto(

        @Schema(title = "Идентификатор", description = "Идентификатор поездки")
        UUID id,

        @Schema(title = "HRI", description = "Человекочитаемый идентификатор поездки")
        String humanReadableId,

        @Schema(title = "HRI маршрутного листа", description = "Человекочитаемый идентификатор маршрутного листа")
        String routeHumanReadableId,

        @Schema(title = "Статус", description = "Статус поездки")
        TripStatus status,

        @Schema(title = "Заявки", description = "Список заявок, участвующих в поездке")
        List<Map<String, Object>> requests,

        @Schema(title = "Дополнительные данные", description = "Грузоподъемность тс")
        Double capacity,

        @Schema(title = "Дополнительные данные", description = "Время работы грузчиков")
        Long loadersWorkTime,

        @Schema(title = "Остановки", description = "Список остановок посадки/высадки пассажиров")
        List<RequestDto.Waypoint> waypoints,

        @Schema(title = "Начало", description = "Время начала поездки")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime startTime,

        @Schema(title = "Начало по диспетчеру", description = "Время начала поездки по диспетчеру")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime dispatcherStartTime,

        @Schema(title = "Окончание", description = "Время окончания поездки. Если поездка не завершена, рассчетное время окончания поездки")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime endTime,

        @Schema(title = "Водитель", description = "Базовые данные водителя")
        DriverShortDTO driver,

        @Schema(title = "Автомобиль", description = "Базовые данные автомобиля")
        VehicleDTO vehicle,

        @Schema(title = "Диспетчер", description = "Базовые данные диспетчера")
        DispatcherShortDTO dispatcher,

        @Schema(title = "Контрагент", description = "Идентификатор контрагента")
        UUID contractorId,

        @Schema(title = "Дистанция", description = "Фактическая дистанция поездки")
        Double factDistance,

        @Schema(title = "Новизна", description = "Флаг новизны поездки")
        boolean isNew,

        @Schema(title = "Стоимость", description = "Плановая стоимость поездки (копейки)")
        Long expectedCost,

        @Schema(title = "Фактическая стоимость", description = "Фактическая стоимость поездки (копейки)")
        Integer factCost,

        @Schema(title = "Время доставки", description = "Время доставки с часовым поясом")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime finishTime,

        @Schema(title = "Расстояние", description = "Плановое расстояние поездки (километры)")
        Double expectedDistance,

        @Schema(title = "План", description = "Плановые данные по водителю и автомобилю")
        Planned planned,

        @Schema(title = "Время", description = "Время создания поездки")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime creationTime,
        @Schema(title = "Время ожидания", description = "Время ожидания водителем")
        Long driverWaitingTime,

        @Schema(title = "Количество грузчиков", description = "Количество грузчиков на маршруте")
        Integer loaders,

        @Schema(title = "Комментарий", description = "Комментарий к поездке")
        String comment,

        @Schema(title = "Имя автора", description = "ФИО автора маршрута")
        String authorName,

        @Schema(title = "Телефон автора", description = "Номер телефона автора маршрута")
        String authorPhone
) implements TripDataDto {

        public record Planned(
                @Schema(title = "Водитель", description = "Плановые данные водителя")
                DriverShortDTO driver,

                @Schema(title = "Автомобиль", description = "Плановые данные автомобиля")
                VehicleDTO vehicle
        ){ }
}
