package ru.sber.transport.notifications.database.model.contractor;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import ru.sber.transport.notifications.database.model.HasEmail;
import ru.sber.transport.notifications.database.model.HasId;
import ru.sber.transport.notifications.database.model.HasPhone;

import java.util.UUID;

@Entity
@Table(schema = "notifications_request", name = "driver")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver implements HasId, HasPhone, HasEmail {
    
    /** ID водителя */
    @Id
    private UUID id;
    
    /** Контрагент */
    @Column(name = "contractor_id")
    private UUID contractorId;
    
    /** Имя водителя */
    @JsonAlias("firstname")
    @Column(name = "first_name")
    private String firstName;
    
    /** Фамилия водителя */
    @JsonAlias("lastname")
    @Column(name = "last_name")
    private String lastName;
    
    /** Отчество водителя */
    @Column
    private String patronymic;
    
    /** Автопарк */
    @Column(name = "autopark_id")
    private UUID autoparkId;
    
    /**
     * Рейтинг водителя
     */
    @Column(name="rating")
    @Min(0)
    @Max(500)
    @Builder.Default
    private Integer rating = 500;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Builder.Default
    @Column(name = "phone_confirmed")
    private boolean phoneConfirmed = false;
}
