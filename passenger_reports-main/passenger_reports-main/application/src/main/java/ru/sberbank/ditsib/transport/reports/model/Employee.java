package ru.sberbank.ditsib.transport.reports.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.reports.dto.EmployeeDTO;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Модель сотрудника
 */
@Entity
@Table(schema = "reports", name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
@EqualsAndHashCode(of = "id")
@SqlResultSetMapping(
        name = "EmployeeDTO",
        classes = {
                @ConstructorResult(
                        targetClass = EmployeeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "human_readable_id"),
                                @ColumnResult(name = "user_id", type = UUID.class),
                                @ColumnResult(name = "personnel_number"),
                                @ColumnResult(name = "first_name"),
                                @ColumnResult(name = "last_name"),
                                @ColumnResult(name = "patronymic"),
                                @ColumnResult(name = "itinerant_type"),
                                @ColumnResult(name = "marriage_certificate_number"),
                                @ColumnResult(name = "position_id", type = UUID.class),
                                @ColumnResult(name = "position_name"),
                                @ColumnResult(name = "organization_id", type = UUID.class),
                                @ColumnResult(name = "department_id", type = UUID.class),
                                @ColumnResult(name = "mobile_phone")
                        }) })
public class Employee {
    
    /**
     * Id сотрудника
     */
    @Id
    private UUID id;
    
    /**
     * Personnel number
     */
    @Column(name = "personnel_number")
    private String personnelNumber;
    
    /**
     * Человеко-читаемый ID
     */
    @Column(name = "human_readable_id")
    private String humanReadableId;
    
    /**
     * Имя
     */
    @Column(name = "first_name")
    private String firstName;
    
    /**
     * Фамилия
     */
    @Column(name = "last_name")
    private String lastName;
    
    /**
     * Отчество
     */
    @Column
    private String patronymic;
    
    @Column(name = "mobile_phone")
    private String mobilePhone;
    
    /**
     * Департамент сотрудника
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    /**
     * Должность сотрудника
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    /**
     * Тип разъездного характера сотрудника
     */
    @Column(name = "itinerant_type")
    @Enumerated(EnumType.STRING)
    private ItinerantType itinerantType;
    
    @Column(name = "cost_center")
    private String costCenter;
    
    @Column(name = "marriage_certificate_number")
    private String marriageCertificateNumber;
    
    @Column(name = "user_id")
    private UUID userId;
    
    public Employee(UUID id) {
        this.id = id;
    }
    
    public String getFIO() {
        return getLastName() + " " + getFirstName() + (getPatronymic() == null ? "" : (" " + getPatronymic()));
    }
}
