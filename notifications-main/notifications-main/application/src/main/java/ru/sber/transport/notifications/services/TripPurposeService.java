package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.TripPurpose;

import java.util.UUID;

/**
 * Сервис для работы с целями поездок.
 */
public interface TripPurposeService {
    
    /**
     * Получение цели поездки.
     *
     * @param purposeId идентификатор цели.
     * @return цель поездки.
     */
    TripPurpose get(UUID purposeId);
    
    /**
     * Сохранение цели поездки.
     *
     * @param purpose цель для сохранения.
     * @return сохраненная цель.
     */
    TripPurpose save(TripPurpose purpose);
}
