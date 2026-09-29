package ru.sber.transport.telemechanic.model;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Person {
    private String firstName;
    private String lastName;
    private String patronymic;
    
    public Person(String fullName) throws IllegalArgumentException {
        if (fullName == null || fullName.isEmpty()) {
            this.firstName = "";
            this.lastName = "";
            this.patronymic = "";
        } else {
            var parts = fullName.trim().split(" ");
            
            if (parts.length == 1) {
                this.firstName = "";
                this.lastName = parts[0];
                this.patronymic = "";
            } else if (parts.length == 2) {
                this.firstName = parts[1];
                this.lastName = parts[0];
                this.patronymic = "";
            } else if (parts.length > 2) {
                this.firstName = parts[1];
                this.lastName = parts[0];
                this.patronymic = parts[2];
            }
        }
    }
}

