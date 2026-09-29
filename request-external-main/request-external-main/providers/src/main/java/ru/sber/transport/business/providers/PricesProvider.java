package ru.sber.transport.business.providers;

import java.util.List;
import ru.sber.transport.request.external.model.PriceData;
import ru.sber.transport.request.external.model.Tariff;
import ru.sber.transport.request.external.model.waypoint.WaypointDTO;

/**
 * Провайдер работы с ценами
 */
public interface PricesProvider {

    /**
     * Возвращает цену за маршрут
     *
     * @param waypoints маршрут
     * @param tariff    тариф
     * @return цена
     */
    PriceData get(List<WaypointDTO> waypoints, Tariff tariff);

}
