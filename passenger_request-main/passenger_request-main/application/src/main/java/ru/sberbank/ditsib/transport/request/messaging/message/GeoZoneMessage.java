package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Сообщение геозоны.
 *
 * @deprecated переходим к исползованию utils-kafka
 */
@Getter
@Setter
@Deprecated(forRemoval = true)
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
     * Признак удаления.
     */
    private boolean deleted;
    
}
