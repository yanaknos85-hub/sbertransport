package ru.sberbank.ditsib.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

/**
 * Подразделение
 */
@Entity
@Table(name = "department")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Department {

    /**
     * Идентификатор записи о подразделении
     */
    @Id
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, referencedColumnName = "id")
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
    private String departmentName;


    /**
     * Флаг активности
     */
    @Builder.Default
    private boolean active = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
