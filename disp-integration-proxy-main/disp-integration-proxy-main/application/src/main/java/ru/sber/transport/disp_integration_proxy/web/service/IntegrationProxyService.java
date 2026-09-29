package ru.sber.transport.disp_integration_proxy.web.service;

import java.util.Map;

/**
 * Сервис для проксирования запросов
 */
public interface IntegrationProxyService {

    Object auth();

    Object createTrip(String transportationType, Object body);

    Object getTrip(String transportationType, String orderId);

    Object cancelTrip(String transportationType, String orderId);

}
