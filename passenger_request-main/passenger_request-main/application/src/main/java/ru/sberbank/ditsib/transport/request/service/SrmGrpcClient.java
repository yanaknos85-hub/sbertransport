package ru.sberbank.ditsib.transport.request.service;

import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.request.dto.CancelSharedRideDTO;

import java.util.UUID;

public interface SrmGrpcClient {
    /**
     * Отмена заказа из совместной поездкии
     *
     * @param requestId номер заказа для отмены
     *
     * @return DTO с данными созданной поездки
     */
    CancelSharedRideDTO cancelRequestGrpc(UUID requestId);
    
    /**
     * Получение совместной поездки по id
     *
     * @param rideId id совместной поездки
     *
     * @return DTO с данными совместной поездки
     */
    SrmSharedRideDTO getSharedRideGrpc(UUID rideId);
}


