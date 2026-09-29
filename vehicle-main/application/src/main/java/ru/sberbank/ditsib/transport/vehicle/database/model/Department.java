package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Подразделение
 */
@Entity
@Table(name = "department")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Department {

    /**
     * Идентификатор записи о подразделении
     */
    @Id
    @Setter
    private UUID id;

    /**
     * Человекочитаемый идентификатор
     */
    @NotBlank
    private String humanReadableId;

    /**
     * Идентификатор записи об организации
     */
    @NotNull
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;

    /**
     * Идентификатор записи родителя в таблице department
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Department parent;

    /**
     * Наименование подразделения
     */
    @NotBlank
    @Setter
    private String departmentName;

    /**
     * Флаг активности
     */
    @Setter
    @Builder.Default
    private boolean active = true;
    
    /**
     * Орг. единица
     */
    private String easupId;
}
