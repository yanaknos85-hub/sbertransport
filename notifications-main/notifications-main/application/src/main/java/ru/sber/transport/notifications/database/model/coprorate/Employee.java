package ru.sber.transport.notifications.database.model.coprorate;

import lombok.*;
import ru.sber.transport.notifications.database.model.HasEmail;
import ru.sber.transport.notifications.database.model.HasId;
import ru.sber.transport.notifications.database.model.HasPhone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(schema = "notifications_corporate", name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public class Employee implements HasId, HasPhone, HasEmail {
    
    @Id
    private UUID id;
    
    @Column(name = "user_id", unique = true)
    private UUID userId;
    
    @Column(name = "department_id")
    private UUID departmentId;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "patronymic")
    private String patronymic;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone")
    private String phone;

    @Builder.Default
    @Column(name = "phone_confirmed")
    private boolean phoneConfirmed = false;

    @Column(name = "organization_id")
    private UUID organizationId;

}
