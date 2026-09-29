package ru.sber.transport.request.external.messaging.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.messaging.Message;

@Value
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationMessage implements Message<UUID> {
    /**
     * Идентификатор
     */
    UUID id;
    /**
     * Идентификаторы получателей
     */
    List<UUID> receivers;
    /**
     * Тип сообщения
     */
    String messageType;
    /**
     * Тип приложения
     */
    String applicationType;
    /**
     * Данные о заявке
     */
    List<Data> data;

    @Value
    @Builder
    public static class Data {

        String key;

        Object value;
    }
}
