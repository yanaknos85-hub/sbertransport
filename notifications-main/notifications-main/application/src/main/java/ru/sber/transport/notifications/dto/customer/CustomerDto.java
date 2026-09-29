package ru.sber.transport.notifications.dto.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.*;
import ru.sber.transport.notifications.database.model.HasEmail;
import ru.sber.transport.notifications.database.model.HasId;
import ru.sber.transport.notifications.database.model.HasPhone;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CustomerDto implements HasId, HasPhone, HasEmail {
    
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

    @Column(name = "organization_id")
    private UUID organizationId;

    /**
     * Не понятно для чего этот класс,пока всегда возвращает true
     * @return телефон подтвержден
     */
    @Override
    public boolean isPhoneConfirmed() {
        return true;
    }
}