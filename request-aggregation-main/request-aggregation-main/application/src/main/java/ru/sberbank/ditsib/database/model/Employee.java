package ru.sberbank.ditsib.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sberbank.ditsib.enumerate.ItinerantType;

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
    @NotBlank
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

    @Column(name = "cost_center")
    private String costCenter;

    @Column(name = "itinerant_type")
    @Enumerated(EnumType.STRING)
    private ItinerantType itinerantType;

    /**
     * Флаг активности
     */
    @Builder.Default
    private boolean active = true;

    public String getFIO() {
        return getLastName() + " " + getFirstName() + (getPatronymic() == null ? "" : (" " + getPatronymic()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee employee)) return false;
        return Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
