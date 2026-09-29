package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accessible_position",
        uniqueConstraints = @UniqueConstraint(columnNames = "title"))
@Getter
public class AccessiblePosition {
    /**
     * Идентификатор должности
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Наименование должности
     */
    @NotBlank
    @Size(min = 1, max = 255)
    private String title;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AccessiblePosition address = (AccessiblePosition) o;
        return id != null && Objects.equals(id, address.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
