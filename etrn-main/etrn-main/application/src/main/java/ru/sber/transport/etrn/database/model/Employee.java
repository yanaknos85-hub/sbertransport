package ru.sber.transport.etrn.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

/**
 * Entity describing employee
 */
@NamedEntityGraph(
        name = "employeeWithDepartment",
        attributeNodes = {
                @NamedAttributeNode(value = "department")
        }
)
@Entity
@Table(schema = "etrn_cargo", name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Employee {

    @Id
    private UUID id;

    @Column(name = "humanreadableid")
    private String humanReadableId;

    /**
     * First name
     */
    @Column(name = "first_name", nullable = false)
    @NotBlank
    private String firstName;

    /**
     * Last name
     */
    @Column(name = "last_name", nullable = false)
    @NotBlank
    private String lastName;

    /**
     * Patronymic
     */
    @Column
    private String patronymic;

    /**
     * Personnel number
     */
    @Column(name = "personnel_number")
    private String personnelNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentId")
    private Department department;

    /**
     * Id of linked user
     */
    @Column(name = "user_id", unique = true)
    private UUID userId;

    /**
     * Флаг активности(false - удален, true - активен)
     */
    @Builder.Default
    @Column
    private boolean active = true;
}