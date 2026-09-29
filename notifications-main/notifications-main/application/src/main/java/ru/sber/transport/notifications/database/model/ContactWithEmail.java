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
public class ContactWithEmail implements HasEmail, HasId {

    private UUID id;
    private String email;
}
