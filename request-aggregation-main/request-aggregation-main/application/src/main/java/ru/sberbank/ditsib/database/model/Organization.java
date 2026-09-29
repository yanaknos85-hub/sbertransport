package ru.sberbank.ditsib.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

/**
 * Организация
 */
@With
@Entity
@Table(name = "organization")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Organization {
    
    /**
     * Идентификатор записи об организации
     */
    @Id
    @Setter
    private UUID id;
    
    /**
     * Уникальный идентификатор (числовой)
     */
    @Column(columnDefinition = "numeric")
    private Long digitId;
    
    /**
     * Служебное название
     */
    private String officialName;
    
    /**
     * Флаг активности
     */
    @Setter
    @Builder.Default
    private boolean active = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Organization that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
