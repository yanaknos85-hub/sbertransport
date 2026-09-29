package ru.sberbank.ditsib.transport.request.database.model.corp;

import java.util.UUID;

public final class TechnicalUser {
    private static Employee user = Employee.builder()
                                           .id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                           .firstName("Technical")
                                           .lastName("User")
                                           .userId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                           .build();
    public static Employee get() {
        return user;
    };
}
