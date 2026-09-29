package ru.sber.transport.request.external.web.model;

import java.math.BigDecimal;
import ru.sber.transport.request.external.model.WaypointData;

/**
 * Объект ответа с путевыми точками
 */
public class WebResponseWaypoint extends ru.sber.transport.web.model.Waypoint {

    private final WaypointData delegatee;

    /**
     * Конструктор
     *
     * @param delegatee - объект путевой точки
     */
    WebResponseWaypoint(WaypointData delegatee) {
        this.delegatee = delegatee;
    }

    @Override
    public BigDecimal getLatitude() {
        return delegatee.getLatitude();
    }

    @Override
    public BigDecimal getLongitude() {
        return delegatee.getLongitude();
    }

    @Override
    public String getBuilding() {
        return delegatee.getBuilding();
    }

    @Override
    public String getCity() {
        return delegatee.getCity();
    }

    @Override
    public String getCountry() {
        return delegatee.getCountry();
    }

    @Override
    public String getHouse() {
        return delegatee.getHouse();
    }

    @Override
    public String getRegion() {
        return delegatee.getRegion();
    }

    @Override
    public String getStreet() {
        return delegatee.getStreet();
    }

    @Override
    public String getStructure() {
        return delegatee.getStructure();
    }
}
