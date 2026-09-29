package ru.sber.transport.trips.cargo.business.model;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public final class Employee implements HasName{
    private UUID id;
    private String lastName;
    private String firstName;
    private String patronymic;
    private String mobilePhone;
    private String email;
    private String organization;
}
