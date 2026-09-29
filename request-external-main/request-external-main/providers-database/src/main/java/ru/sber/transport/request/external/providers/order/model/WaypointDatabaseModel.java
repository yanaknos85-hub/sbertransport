package ru.sber.transport.request.external.providers.order.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.database.external_request.tables.records.WaypointRecord;
import ru.sber.transport.request.external.model.WaypointData;

/**
 * Класс описывает модель точки маршрута.
 */
@RequiredArgsConstructor
public class WaypointDatabaseModel implements WaypointData {

    @Delegate
    private final WaypointRecord delegatee;
}
