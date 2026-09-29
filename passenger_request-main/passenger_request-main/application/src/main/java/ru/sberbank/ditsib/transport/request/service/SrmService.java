package ru.sberbank.ditsib.transport.request.service;

import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с маджентой
 */
public interface SrmService {
    
    /**
     * Публикация новой совместной поездки
     *
     * @param sharedRidePostDTO данные поездки
     * @param token токен авторизации
     *
     * @return DTO с данными созданной поездки
     */
    SrmSharedRideDTO postNewSharedRide(SrmRequestDTO sharedRidePostDTO, String token);
    
    /**
     * Запрос на присоединение к совместной поездке
     *
     * @param requestId идентификатор существующей совместной поездки
     * @param sharedRidePostDTO данные нового заказа
     * @param token токен авторизации
     *
     * @return DTO с данными созданной поездки
     */
    SrmSharedRideDTO addRequestToSharedRide(UUID requestId, SrmRequestDTO sharedRidePostDTO, String token);
    
    /**
     * Получение списка подходящих совместных поездок
     *
     * @param sharedRidePostDTO данные новой поездки
     * @param token токен авторизации
     *
     * @return DTO с предварительными данными подходящих объединенных поездок
     */
    List<SrmSharedRideDTO> getSuitableSharedRides(SrmRequestDTO sharedRidePostDTO, String token);
    
    /**
     * Получение совместной поездки по id
     *
     * @param requestId id совместной поездки
     * @param token токен авторизации
     *
     * @return DTO с данными совместной поездки
     */
    Optional<SrmSharedRideDTO> getSharedRideByRequestId(UUID requestId, String token);
}
