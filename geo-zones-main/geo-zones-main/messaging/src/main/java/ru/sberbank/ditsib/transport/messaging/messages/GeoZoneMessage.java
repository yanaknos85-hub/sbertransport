package ru.sberbank.ditsib.transport.messaging.messages;

import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Сообщение геозоны.
 */
@Getter
@Setter
public class GeoZoneMessage implements Message<UUID> {
    
    /**
     * Идентификатор.
     */
    private UUID id;
    
    /**
     * Код.
     */
    private String code;
    
    /**
     * Название.
     */
    private String name;
    
    /**
     * Идентификатор родителя.
     */
    private UUID parentId;

    /**
     * Временная зона
     */
    private String timeZone;

    /**
     * Признак удаления.
     */
    private boolean deleted;
    
}
