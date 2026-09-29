package ru.sber.transport.authsb.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Сущность для таблицы roles — хранит роли пользователей.
 */
@Entity
@Table(name = "roles", schema = "authorization_sbid")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_role_user_id"))
    private User user;

    @Column(name = "role", length = 11, nullable = false)
    private String role;
}
