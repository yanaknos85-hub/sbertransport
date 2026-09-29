package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Сущность "Пользователь" (users)
 * Представляет пользователя системы — участника биржи грузоперевозок.
 * Хранится в таблице 'users' схемы 'exchange_request'.
 */
@Entity
@Table(
    schema = "exchange_request",
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone")
    },
    // Валидация email через регулярное выражение
    indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_phone", columnList = "phone")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Уникальный идентификатор пользователя (UUID)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Электронная почта пользователя.
     * Обязательна, уникальна, формат проверяется регулярным выражением.
     * Пример: ivanov@example.com
     */
    @Column(
        name = "email",
        nullable = false,
        unique = true,
            columnDefinition = "VARCHAR(255) CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$')"
    )
    private String email;

    /**
     * Телефонный номер пользователя.
     * Может отсутствовать, но если указан — должен быть уникальным и соответствовать формату.
     * Формат: +79161234567, допускаются пробелы и дефисы.
     */
    @Column(
        name = "phone",
        unique = true,
        length = 20,
        columnDefinition = "VARCHAR(20) CHECK (phone ~ '^+?[0-9\\s-]+$')"
    )
    private String phone;

    /**
     * Связь с сущностью Organization (опционально, если нужно навигационное поле)
     * FetchType.LAZY — подгружается по требованию
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    private Organization organization;
}


