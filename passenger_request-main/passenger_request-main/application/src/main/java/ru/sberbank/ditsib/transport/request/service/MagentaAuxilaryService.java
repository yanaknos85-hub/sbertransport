package ru.sberbank.ditsib.transport.request.service;

import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.request.database.model.AbstractRequestForTnP;

import java.util.UUID;

/**
 * Имплементация сервиса для работы с маджента
 */
public interface MagentaAuxilaryService {
    
    /**
     * Метод комбинирующий создание новой поездки в Мадженте(если rideId = null)
     * или присоединение к существующей поездке в Мадженте с rideId.
     * Отправляет запрос на отмену заявки в случае ошибки.
     * @param rideId Номер совместной поездки в Мадженте
     * @param savedRequest Заявка в TRANSPORT
     * @return MagentaSharedRequest
     */
    SrmSharedRideDTO processCoopRequest(UUID rideId, AbstractRequestForTnP savedRequest, String token);
}
