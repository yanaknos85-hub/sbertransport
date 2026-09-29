package ru.sberbank.ditsib.transport.request.service;

import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingInfoRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingInfoResponseDTO;

import java.util.UUID;

/**
 * Сервис для работы с доп информацией по пользователю каршеринга (ПНД итд)
 */
public interface CarsharingInfoService {
    
    /**
     * Получить доп информацию по пользователю каршеринга
     *
     * @return {@link CarsharingInfoResponseDTO}
     */
    CarsharingInfoResponseDTO get();
    
    /**
     * Обновить доп информацию по пользователю каршеринга
     *
     * @param dto {@link CarsharingInfoRequestDTO}
     *
     * @return обновлённый {@link CarsharingInfoResponseDTO}
     */
    CarsharingInfoResponseDTO update(CarsharingInfoRequestDTO dto);
    
    /**
     * Получение deeplink в зависимости от request
     *
     * @param requestId Номер заявки
     *
     * @return deeplink
     */
    @NonNull
    String getDeeplinkByRequestId(@NonNull UUID requestId);
}
