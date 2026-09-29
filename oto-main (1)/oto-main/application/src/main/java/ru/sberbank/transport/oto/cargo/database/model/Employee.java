package ru.sberbank.transport.oto.cargo.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.ItinerantType;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Модель сотрудника
 */
@Entity
@Table(schema = "oto_cargo", name = "employee")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
public class Employee {
    
    /**
     * Id сотрудника
     */
    @Id
    @Column
    private UUID id;
    
    /**
     * Id of linked user
     */
    @Column(name = "user_id")
    private UUID userId;

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
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    /**
     * Должность сотрудника
     */
    @ManyToOne
    @JoinColumn(name = "position_id")
    private Position position;
    
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

    public String getFIO() {
        return getLastName() + " " + getFirstName() + (getPatronymic() == null ? "" : (" " + getPatronymic()));
    }
}
