package ru.sber.transport.notifications.database.model.contractor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sber.transport.notifications.database.model.HasEmail;
import ru.sber.transport.notifications.database.model.HasId;
import ru.sber.transport.notifications.database.model.HasPhone;

import java.util.UUID;

@Entity
@Table(schema = "notifications_contractor", name = "dispatcher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispatcher implements HasId, HasPhone, HasEmail {

    /**
     * Id диспетчера
     */
    @NotNull
    @Id
    private UUID id;

    /**
     * Id контрагента
     */
    @NotNull
    @Column(name = "contractor_id", nullable = false)
    private UUID contractorId;

    /**
     * телефон
     */
    @Column(name = "phone")
    private String phone;

    /**
     * почта
     */
    @Column(name = "email")
    private String email;

    /**
     * признак, что телефон подтвержден
     */
    @Builder.Default
    @Column(name = "phone_confirmed")
    private boolean phoneConfirmed = false;

}
