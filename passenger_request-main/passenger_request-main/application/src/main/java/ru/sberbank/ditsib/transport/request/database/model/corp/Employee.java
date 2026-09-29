package ru.sberbank.ditsib.transport.request.database.model.corp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.request.database.model.ApproveDepartment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Entity describing employee
 */
@Entity
@Table(schema = "request", name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Employee {
    
    /**
     * Id of employee
     */
    @Id
    private UUID id;
    
    /**
     * Human readable id
     */
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
     * Id of linked user
     */
    @Column(name = "user_id", unique = true)
    private UUID userId;
    
    /**
     * Personnel number
     */
    @Column(name = "personnel_number")
    private String personnelNumber;
    
    @Column(name = "supervisor_id")
    private UUID supervisorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentId")
    private Department department;
    
    @Column(name = "position_id")
    private UUID positionId;
    
    @Column(name = "mobile_phone")
    private String mobilePhone;

    @Column(name = "cost_center")
    private String costCenter;

    @Column(name = "itinerant_type")
    @Enumerated(EnumType.STRING)
    private ItinerantType itinerantType;

    @Column(name = "marriage_certificate_number")
    private String marriageCertificateNumber;
    
    /**
     * Список подразделений, которые может согласовывать данный сотрудник
     */
    @ElementCollection
    @CollectionTable(schema = "request", name = "approvers",
                     joinColumns = @JoinColumn(name = "employee_id"))
    @Builder.Default
    private Collection<ApproveDepartment> approveDepartments = new ArrayList<>();
    
    /**
     * Флаг активности(false - удален, true - активен)
     */
    @Builder.Default
    @Column
    private boolean active = true;
    
    public Employee(UUID id) {
        this.id = id;
    }
    
    public String getFIO() {
        return getLastName() + " " + getFirstName() + (getPatronymic() == null ? "" : (" " + getPatronymic()));
    }
    
    public String getIO() {
        return getFirstName() + (getPatronymic() == null ? "" : (" " + getPatronymic()));
    }
}
