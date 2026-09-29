package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

/**
 * Организация
 */
@Entity
@Table(name = "organization")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
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
    @Column
    @Builder.Default
    private boolean active = true;
}
