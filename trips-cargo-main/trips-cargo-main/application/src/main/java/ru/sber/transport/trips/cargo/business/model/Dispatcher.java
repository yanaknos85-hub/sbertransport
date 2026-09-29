package ru.sber.transport.trips.cargo.business.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Dispatcher implements HasName{

    private UUID id;

    private String humanReadableId;

    private String lastName;

    private String firstName;

    private String patronymic;

    private String phone;

    private String email;

    private UUID contractorId;

    private boolean active;

    private boolean consent;

    private UUID oauthId;

    private UUID autoparkId;
}
