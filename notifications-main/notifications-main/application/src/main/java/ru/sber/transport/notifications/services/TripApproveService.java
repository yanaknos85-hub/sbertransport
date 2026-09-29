package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.approve.TripApprove;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с согласованиями поездок.
 */
public interface TripApproveService {
    
    /**
     * Получение согласования по ID поездки.
     *
     * @param id идентификатор поездки.
     * @return согласование.
     */
    TripApprove get(UUID id);
    
    /**
     * Сохранение информации по согласованию.
     *
     * @param tripApprove согласование.
     * @return сохраненное согласование.
     */
    TripApprove save(TripApprove tripApprove);
    
    Optional<TripApprove> getByRequestId(UUID id);
}
