package ru.sber.transport.notifications.database.model.settings.restriction;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Сущность ролей.
 */
@Entity
@Getter
@Setter
@Table(schema = "notifications_corporate", name = "roles")
@ToString
@EqualsAndHashCode
public class Role {
    
    @Id
    private String code;
    
}
