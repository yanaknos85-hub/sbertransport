package ru.sber.transport.notifications.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(schema = "notifications", name = "contacts")
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationContact {

    @Id
    private UUID id;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Builder.Default
    @Column(name = "phone_confirmed")
    private boolean phoneConfirmed = false;
}
