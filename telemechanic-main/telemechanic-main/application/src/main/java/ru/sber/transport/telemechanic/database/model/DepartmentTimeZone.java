package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

import org.hibernate.Hibernate;

/**
 * Часовой пояс подразделения
 */
@Entity
@Table(name = "department_time_zone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentTimeZone {

    /**
     * Идентификатор записи
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Идентификатор подразделения
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false, referencedColumnName = "id")
    private Department department;

    /**
     * Часовой пояс в формате UTC+03:00
     */
    @NotBlank
    private String timeZone;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (DepartmentTimeZone) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
