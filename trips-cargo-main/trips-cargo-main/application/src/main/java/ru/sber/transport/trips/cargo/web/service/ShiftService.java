package ru.sber.transport.trips.cargo.web.service;

import ru.sber.transport.trips.cargo.business.model.Shift;

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
