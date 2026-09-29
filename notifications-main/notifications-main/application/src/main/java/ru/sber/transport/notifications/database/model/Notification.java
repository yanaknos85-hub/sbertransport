package ru.sber.transport.notifications.database.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.dto.customer.CustomerDto;

import java.util.UUID;

/**
 * Сущность уведомления.
 */
@Entity
@Table(schema = "notifications", name = "notification")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Column(name = "entity_id")
    private UUID entityId;

    @Type(JsonBinaryType.class)
    @Column
    private String entity;

    @Column(nullable = false)
    private boolean sent;

    @Column(nullable = false)
    private boolean sentError;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settings_id", nullable = false)
    private NotificationSettings settings;

    @Transient
    private CustomerDto customer;

    /**
     * Parse an object from json.
     *
     * @param classType class of object.
     * @param <T> type of object.
     * @return parsed object.
     * @throws JsonProcessingException object parsing failed.
     */
    public <T> T getEntityFromJson(Class<T> classType) throws JsonProcessingException {
        if (entity == null) {
            return null;
        }
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        return objectMapper.readValue(entity, classType);
    }
}
