package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.dto.TripPurposeDTO;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with trip purposes.
 */
public interface TripPurposeService {

    /**
     * Получение цели поездки.
     *
     * @param uuid ID of trip purpose.
     *
     * @return trip purpose dto.
     */
    Optional<TripPurpose> get(UUID uuid);


    /**
     * Сохранение цели поездки.
     *
     * @return trip purpose dto.
     */
    TripPurposeDTO save(TripPurpose tripPurposeDTO);

    /**
     * Получаем часто используемые причины поездок по пользователю
     * @param userId Идентификатор записи пользователя с таблицы corporate.users
     * @return {@link TripPurpose}
     */
    TripPurposeDTO getFrequentlyUsedTripPurpose(UUID userId);
}
