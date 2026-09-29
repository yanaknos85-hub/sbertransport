package ru.sber.transport.authentication.providers.grpc.notification;

import ru.sber.transport.authentication.business.dto.SendingChannel;

import java.util.Map;
import java.util.UUID;

/**
 * Клиент для работы с сервисом отправки уведомлений по grpc
 */
public interface NotificationsGrpcClient {

    /**
     * Отправить уведомление
     */
    void send(UUID userId, String template, Map<String, Object> data, SendingChannel channel);
}