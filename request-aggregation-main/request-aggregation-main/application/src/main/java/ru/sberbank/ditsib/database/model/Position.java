package ru.sberbank.ditsib.database.model;

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
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Position {

    /**
     * Идентификатор записи о должности
     */
    @Id
    private UUID id;

    /**
     * Идентификатор записи об организации
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, referencedColumnName = "id")
    private Organization organization;

    /**
     * Наименование позиции
     */
    @NotBlank
    private String positionName;

    /**
     * Флаг активности
     */
    @Builder.Default
    private boolean active = true;

}
