package ru.sberbank.ditsib.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * История изменений статуса заявки.
 * Представляет собой журнал изменений статусов заявок
 * с указанием времени изменения и переходов между статусами.
 */
@Entity
@Table(name = "status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistory {

    /**
     * Уникальный идентификатор записи истории.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Дата и время изменения статуса.
     */
    @Column(nullable = false)
    private LocalDateTime changeDatetime;

    /**
     * Предыдущий статус заявки.
     */
    @Column(nullable = false)
    private String previousStatus;

    /**
     * Новый статус заявки.
     */
    @Column(nullable = false)
    private String nextStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusHistory that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
