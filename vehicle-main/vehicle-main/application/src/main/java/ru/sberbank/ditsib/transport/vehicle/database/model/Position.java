package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Должность
 */
@Entity
@Table(name = "position")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Position {
    
    /**
     * Идентификатор записи о должности
     */
    @Id
    @Setter
    private UUID id;
    
    /**
     * Идентификатор записи об организации
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;
    
    /**
     * Наименование позиции
     */
    @NotBlank
    @Setter
    private String positionName;
    
    /**
     * Флаг активности
     */
    @Setter
    @Builder.Default
    private boolean active = true;
    
}
