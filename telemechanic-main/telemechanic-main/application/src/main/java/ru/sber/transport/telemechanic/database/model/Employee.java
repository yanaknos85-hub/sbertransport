package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

/**
 * Сотрудник
 */
@Entity
@Table(name = "employee")
@With
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Employee {

    /**
     * Идентификатор записи о сотруднике
     */
    @Id
    private UUID id;

    /**
     * Человекочитаемый идентификатор
     */
    private String humanReadableId;

    /**
     * Имя
     */
    @NotBlank
    private String firstName;

    /**
     * Фамилия
     */
    @NotBlank
    private String lastName;

    /**
     * Отчество
     */
    private String patronymic;

    /**
     * Идентификатор записи с таблицы corporate.user
     */
    @NotNull
    private UUID userId;

    /**
     * Табельный номер
     */
    @NotBlank
    private String personnelNumber;

    /**
     * Идентификатор записи о подразделении
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false, referencedColumnName = "id")
    private Department department;

    /**
     * Идентификатор записи о должности
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id", nullable = false, referencedColumnName = "id")
    private Position position;

    /**
     * Идентификатор записи о организации
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, referencedColumnName = "id")
    private Organization organization;

    /**
     * Номер телефона
     */
    private String mobilePhone;

    /**
     * Флаг активности
     */
    @Builder.Default
    private boolean active = true;
    
    /**
     * Индекс для поиска по ФИО
     */
    private String fullNameIndex;

    public String getFIO() {
        return getLastName() + " " + getFirstName() + (getPatronymic() == null ? "" : (" " + getPatronymic()));
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (Employee) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
