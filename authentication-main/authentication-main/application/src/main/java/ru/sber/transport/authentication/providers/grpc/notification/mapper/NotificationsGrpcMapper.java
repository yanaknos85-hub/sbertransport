package ru.sber.transport.authentication.providers.grpc.notification.mapper;

import com.google.protobuf.NullValue;
import org.mapstruct.Mapper;
import ru.sber.transport.authentication.business.dto.SendingChannel;
import ru.sber.transport.notifications.sync.grpc.service.NotificationsOuterClass;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования данных из/в protobuf для нотфиикаций
 */
@Mapper
public interface NotificationsGrpcMapper {

    /**
     * Маппинг данных для нотификации
     * @param userId id пользователя
     * @param template шаблон сообщения
     * @param data данные сообщения
     * @param channel канал связи
     * @return данных для нотификации
     */
    default NotificationsOuterClass.NotificationData mapNotificationData(UUID userId, String template, Map<String, Object> data, SendingChannel channel) {
        return NotificationsOuterClass.NotificationData.newBuilder()
                .setChannel(mapChannel(channel))
                .setUserId(userId.toString())
                .putAllData(mapData(data))
                .setTemplate(template)
                .build();
    }

    /**
     * Маппинг данных на proto
     * @param data мапа с данными
     * @return proto мапа
     */
    default Map<String, NotificationsOuterClass.Object> mapData(Map<String, Object> data) {
        if (data == null) {
            return Map.of();
        } else {
            return data.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, kv -> mapObject(kv.getValue())));
        }
    }

    /**
     * Маппинг объекта на proto
     * @param value данные
     * @return proto обхект
     */
    default NotificationsOuterClass.Object mapObject(Object value) {
        return switch (value) {
            case String str -> NotificationsOuterClass.Object.newBuilder()
                    .setString(str)
                    .build();
            case Integer intValue -> NotificationsOuterClass.Object.newBuilder()
                    .setInt(intValue)
                    .build();
            case Long longValue -> NotificationsOuterClass.Object.newBuilder()
                    .setLong(longValue)
                    .build();
            case Double doubleValue -> NotificationsOuterClass.Object.newBuilder()
                    .setDouble(doubleValue)
                    .build();
            case List<?> listVal -> NotificationsOuterClass.Object.newBuilder()
                    .setList(NotificationsOuterClass.List.newBuilder()
                            .addAllItem(listVal.stream().map(this::mapObject).toList())
                            .build()
                    )
                    .build();
            case null -> NotificationsOuterClass.Object.newBuilder()
                    .setNull(NullValue.NULL_VALUE)
                    .build();
            default -> throw new IllegalArgumentException("Unsupported type: " + value.getClass());
        };
    }

    /**
     * Маппинг канала связи на proto
     * @param channel канал
     * @return proto канал
     */
    NotificationsOuterClass.Channel mapChannel(SendingChannel channel);

}