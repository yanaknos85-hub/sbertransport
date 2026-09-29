package ru.sber.transport.request.external.web.model;

import java.util.UUID;
import lombok.experimental.Delegate;
import ru.sber.transport.request.external.model.WaypointData;

/**
 * Класс для запроса путевых точек
 */
public class WebRequestWaypointData implements WaypointData {

    @Delegate
    private final ru.sber.transport.web.model.Waypoint delegatee;

    /**
     * Конструктор для запроса путевых точек
     *
     * @param delegatee - объект путевой точки для запроса
     */
    WebRequestWaypointData(ru.sber.transport.web.model.Waypoint delegatee) {
        this.delegatee = delegatee;
    }

    @Override
    public UUID getId() {
        return null;
    }
}
