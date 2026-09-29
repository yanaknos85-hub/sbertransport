package ru.sber.transport.trip.web.service;

import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Сервис переключения он-лайн статуса водителя.
 */
public interface DriverOnlineSwitcherService {

    /**
     * Изменение параметра выхода на линию водителя диспетчером
     *
     * @param driverId Идентификатор водителя
     * @param authentication Данные диспетчера
     */
    void switchOnline(UUID driverId, Authentication authentication);

}
