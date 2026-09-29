package ru.sber.transport.messaging.messages;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сообщение PUSH-уведомления.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushMessage implements Message<UUID> {

    /**
     * Идентификатор сообщения.
     */
    private UUID id;

    /**
     * Список токенов получателей.
     */
    @Singular("token")
    private List<UUID> receivers;
    
    /**
     * Тип сообщения.
     */
    private String type;
    
    /**
     * Шаблон.
     */
    private String template;
    
    /**
     * Данные.
     */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();
    
    /**
     * Доп. данные.
     */
    @Builder.Default
    private Map<String, Object> additionalData = new HashMap<>();

}
