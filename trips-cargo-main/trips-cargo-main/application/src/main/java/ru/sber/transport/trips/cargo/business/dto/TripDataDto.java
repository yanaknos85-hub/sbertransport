package ru.sber.transport.trips.cargo.business.dto;

import ru.sber.transport.trips.cargo.business.model.TripStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Интерфейс поездок.
 */
public interface TripDataDto {

    /**
     * @return идентификатор.
     */
    UUID id();

    /**
     * @return человекочитаемый идентификатор.
     */
    String humanReadableId();

    /**
     * @return статус.
     */
    TripStatus status();

    /**
     * @return список вложенных заявок.
     */
    List<Map<String, Object>> requests();

    /**
     * @return список вложенных точек.
     */
    List<RequestDto.Waypoint> waypoints();

    /**
     * @return время начала.
     */
    OffsetDateTime startTime();

    /**
     * @return время окончания.
     */
    OffsetDateTime endTime();

    /**
     * @return водитель.
     */
    DriverShortDTO driver();

    /**
     * @return диспетчер.
     */
    DispatcherShortDTO dispatcher();

    /**
     * @return контрагент.
     */
    UUID contractorId();

    /**
     * @return фактическое расстояние.
     */
    Double factDistance();

    /**
     * @return признак новой.
     */
    boolean isNew();

}
