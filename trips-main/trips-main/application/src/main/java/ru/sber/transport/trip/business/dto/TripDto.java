package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.serializer.OffsetDateTimeSerializer;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

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
 * @param passengerCount количество пассажиров.
 * @param taxiClass класс такси.
 * @param waypoints список точек.
 * @param startTime время начала поездки.
 * @param endTime время окончания поездки.
 * @param driver водитель.
 * @param dispatcher диспетчер.
 */
@Schema(title = "Поездка",
        description = "Данные поездки")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TripDto(

        @Schema(title = "Идентификатор", description = "Идентификатор поездки")
        UUID id,

        @Schema(title = "HRI", description = "Человекочитаемый идентификатор поездки")
        String humanReadableId,

        @Schema(title = "Статус", description = "Статус поездки")
        TripStatus status,

        @Schema(title = "Заявки", description = "Список заявок, участвующих в поездке")
        List<Map<String, Object>> requests,

        @Schema(title = "Пассажиры", description = "Количество пассажиров")
        int passengerCount,

        @Schema(title = "Класс такси", description = "Класс такси, используемый в поездке")
        String taxiClass,

        @Schema(title = "Остановки", description = "Список остановок посадки/высадки пассажиров")
        List<RequestDto.Waypoint> waypoints,

        @Schema(title = "Начало", description = "Время начала поездки")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime startTime,

        @Schema(title = "Окончание", description = "Время окончания поездки. Если поездка не завершена, рассчетное время окончания поездки")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime endTime,

        @Schema(title = "Водитель", description = "Базовые данные водителя")
        DriverShortDTO driver,

        @Schema(title = "Диспетчер", description = "Базовые данные диспетчера")
        DispatcherShortDTO dispatcher,

        @Schema(title = "Контрагент", description = "Идентификатор контрагента")
        UUID contractorId,

        @Schema(title = "Время ожидания", description = "Время ожидания пассажира водителем")
        Long driverWaitingTime,

        @Schema(title = "Дистанция", description = "Фактическая дистанция поездки")
        Double factDistance,

        @Schema(title = "Новизна", description = "Флаг новизны поездки")
        boolean isNew
) implements TripDataDto {
}
