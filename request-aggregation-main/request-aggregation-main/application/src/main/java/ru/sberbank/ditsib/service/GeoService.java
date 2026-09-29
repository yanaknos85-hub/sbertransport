package ru.sberbank.ditsib.service;

import ru.sberbank.ditsib.dto.GeoAddress;
import ru.sberbank.ditsib.dto.RouteInfo;

import java.util.List;

/**
 * Сервис для отправки запросов к гео по grpc
 */
public interface GeoService {

    /**
     * Получить информацию о маршруте из гео
     *
     * @param addresses список адресов
     * @return RouteInfo
     */
    RouteInfo getRouteInfo(List<GeoAddress> addresses);

    /**
     * Получить гео адреса по адресу
     * @param address пользовательский адрес
     * @return список гео адресов
     */
    List<GeoAddress> getGeoAddress(String address);

}
