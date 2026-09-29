package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

/**
 * Сотрудник
 */
@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@NamedEntityGraph(name = "with-organization",
        attributeNodes = @NamedAttributeNode(value = "organization")
)
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
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    private Department department;

    /**
     * Идентификатор записи о должности
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id", referencedColumnName = "id")
    private Position position;

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

    /**
     * Идентификатор записи о организации
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;

    public String getFIO() {
        return getLastName() + " " + getFirstName() + (getPatronymic() == null ? "" : (" " + getPatronymic()));
    }

}
