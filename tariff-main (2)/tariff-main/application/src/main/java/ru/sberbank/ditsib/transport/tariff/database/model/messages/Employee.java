package ru.sberbank.ditsib.transport.tariff.database.model.messages;

import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = "tariff", name = "message_employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    
    /**
     * Id of employee
     */
    @Id
    private UUID id;
    
    /**
     * Id of linked user
     */
    @Column(name = "user_id", unique = true)
    private UUID userId;
    
    /**
     * First name
     */
    @Column(name = "first_name")
    private String firstName;
    
    /**
     * Last name
     */
    @Column(name = "last_name")
    private String lastName;
    
    /**
     * patronymic
     */
    @Column
    private String patronymic;
    
    /**
     * Отдел
     */
    @Column(name = "department_id", nullable = false)
    private UUID departmentId;
    
    /**
     * Должность сотрудника
     */
    @JoinColumn(name = "position_id")
    private UUID positionId;
    
    /**
     * Организация
     */
    @Transient
    private UUID organizationId;
}