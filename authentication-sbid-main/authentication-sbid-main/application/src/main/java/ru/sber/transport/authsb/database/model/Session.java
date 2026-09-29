package ru.sber.transport.authsb.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность сессии авторизации через Sber ID.
 */
@Entity
@Table(schema = "authorization_sbid", name = "sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nonce;

    /**
     * Идентификатор сессии
     */
    @Column(nullable = false)
    private String state;

    /**
     * Идентификатор пользователя
     */
    @Column
    private String sub;

    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;

    @Column
    private Boolean active;

    @Column(name = "refresh_token", length = 100)
    private String refreshToken;

    @Column(name = "access_token", length = 100)
    private String accessToken;

    @Column(name = "id_token", length = 1000)
    private String idToken;

    @Column(name = "expires_in")
    private Integer expiresIn;

    @Column
    private String code;
}