package ru.sber.transport.authentication.providers.grpc.notification.impl;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.authentication.business.dto.SendingChannel;
import ru.sber.transport.authentication.providers.grpc.notification.NotificationsGrpcClient;
import ru.sber.transport.authentication.providers.grpc.notification.mapper.NotificationsGrpcMapper;
import ru.sber.transport.authentication.web.exceptions.NotificationsGrpcExceptionFactory;
import ru.sber.transport.notifications.sync.grpc.service.NotificationsGrpc;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationsGrpcClientImpl implements NotificationsGrpcClient {

    @GrpcClient("notifications")
    private NotificationsGrpc.NotificationsBlockingStub blockingStub;

    private final NotificationsGrpcMapper notificationsGrpcMapper;

    @Override
    public void send(UUID userId, String template, Map<String, Object> data, SendingChannel channel) {
        try {
            blockingStub.send(notificationsGrpcMapper.mapNotificationData(userId, template, data, channel));
        } catch (StatusRuntimeException e) {
            throw NotificationsGrpcExceptionFactory.create(e);
        }
    }

}