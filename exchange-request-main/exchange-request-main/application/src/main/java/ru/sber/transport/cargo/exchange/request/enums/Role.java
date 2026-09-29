package ru.sber.transport.cargo.exchange.request.enums;

import lombok.Getter;

@Getter
public enum Role {
    CARRIER("Перевозчик", "перевозчика"),  // Перевозчик
    SHIPPER("Грузовладелец", "грузовладельца"); // Грузовладелец

    private final String displayName;
    private final String displayNameRP;

    Role(String displayName, String displayNameRP) {
        this.displayName = displayName;
        this.displayNameRP = displayNameRP;
    }
}
