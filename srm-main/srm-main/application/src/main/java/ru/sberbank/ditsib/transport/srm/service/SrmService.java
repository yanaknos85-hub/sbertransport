package ru.sberbank.ditsib.transport.srm.service;

import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.srm.dto.internal.SrmMultipleRequestDTO;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с маджентой
 */
public interface SrmService {
    
    /**
     * Публикация новой совместной поездки
     *
     * @param multipleRequestDTO данные поездки
     *
     * @return DTO с данными созданной поездки
     */
    SrmSharedRideDTO addNew(SrmMultipleRequestDTO multipleRequestDTO, UUID bunchId, boolean doSave);
    
    /**
     * Запрос на присоединение к совместной поездке
     *
     * @param rideId идентификатор существующей совместной поездки
     * @param multipleRequestDTO данные нового заказа
     *
     * @return DTO с данными созданной поездки
     */
    SrmSharedRideDTO joinRequest(UUID rideId, SrmMultipleRequestDTO multipleRequestDTO, int bunchNumber);
    
    /**
     * Отмена заказа из совместной поездкии
     *
     * @param requestId номер заказа для отмены
     *
     * @return DTO с данными созданной поездки
     */
    SrmSharedRideDTO cancelRequest(UUID requestId);
    
    /**
     * Получение списка подходящих совместных поездок
     *
     * @param multipleRequestDTO данные поездки для подбора
     *
     * @return DTO с предварительными данными подходящих объединенных поездок
     */
    List<SrmSharedRideDTO> findMatch(SrmMultipleRequestDTO multipleRequestDTO, UUID bunchId, boolean joinCandidate);
    
    /**
     * Получение одной совместной поездки
     *
     * @return DTO с данными поездки
     */
    SrmSharedRideDTO get(UUID rideId);
    
    /**
     * Получение списка совместных поездок
     *
     * @return DTO с данными совместных поездок
     */
    List<SrmSharedRideDTO> getAll();
    
    /**
     * Окончание совместной поездки
     *
     * @return DTO с данными совместной поездки
     */
    SrmSharedRideDTO finishSharedRide(UUID rideId);
    
    /**
     * Получение совместной поездки по id заявки
     *
     * @return DTO с данными совместной поездки
     */
    SrmSharedRideDTO getByRequestId(UUID requestId);
}
