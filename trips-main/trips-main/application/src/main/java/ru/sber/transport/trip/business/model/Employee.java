package ru.sber.transport.trip.business.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class Employee implements HasName{
    private UUID id;
    private String lastName;
    private String firstName;
    private String patronymic;
    private String mobilePhone;
    private String email;
    private String organization;
    private String fullName;
}
