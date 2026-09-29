package ru.sber.transport.notifications.database.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactWithPhoneAndEmail implements HasPhone, HasEmail, HasId {

    private UUID id;

    private String phone;

    @Builder.Default
    private boolean phoneConfirmed = false;

    private String email;
}
