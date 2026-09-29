package ru.sberbank.ditsib.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Сущность правил расчёта маршрутов или условий поездки.
 */
@Entity
@Table(name = "rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rules {
    /**
     * Уникальный идентификатор правила.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    /**
     * Время начала действия правила.
     */
    @NotNull
    private LocalDateTime timeStart;
    /**
     * Геозона, к которой относится правило.
     */
    @Column(nullable = false, columnDefinition = "numeric")
    private Integer geozone;
    /**
     * Максимально допустимое время поездки (в минутах).
     */
    private int maxTimeTravel;
    /**
     * Допустимое отклонение по времени (в минутах).
     */
    private int deviationTime;
    /**
     * Минимально допустимая дистанция в км.
     */
    private int minDstKm;
    /**
     * Максимально допустимая дистанция в км.
     */
    private int maxDstKm;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rules rules)) return false;
        return Objects.equals(id, rules.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
