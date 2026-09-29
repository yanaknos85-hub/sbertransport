package ru.sber.transport.trip.web.service;

import ru.sber.transport.trip.business.model.Shift;

public interface ShiftService {

    /**
     * Деактивация смены
     * @param shift смена
     */
    void deactivate(Shift shift);

    /**
     * Активация смены
     * @param shift смена
     */
    void activate(Shift shift);

}
