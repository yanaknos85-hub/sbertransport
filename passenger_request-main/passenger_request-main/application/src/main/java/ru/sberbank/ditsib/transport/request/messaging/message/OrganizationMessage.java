package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;
import ru.sberbank.ditsib.transport.request.messaging.message.ContactMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сообщение с данными организации.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMessage implements Message<UUID> {
    
    /**
     * Идентификатор организации.
     */
    private UUID id;
    
    /**
     * Цифровой идентификатор.
     */
    private Long digitId;
    
    /**
     * Название организации.
     */
    private String officialName;
    
    /**
     * Адрес организации.
     */
    private String address;
    
    /**
     * ОГРН.
     */
    private String msrn;
    
    /**
     * ИНН.
     */
    private String tid;
    
    /**
     * Список контактов.
     */
    @Builder.Default
    private List<ContactMessage> contacts = new ArrayList<>();
    
    /**
     * Флаг об удалении организации.
     */
    @Builder.Default
    private boolean deleted = false;
    
    /**
     * Группа организаций
     */
    private OrganizationGroup organizationGroup;
    
    public record OrganizationGroup(
            /**
             * ID группы организаций
             */
            UUID id,

            /**
             * Наименование группы организаций
             */
            String name,

            /**
             * Принадлежность к внутренней группе организаций
             */
            boolean internal
    ){}
}
