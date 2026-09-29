package ru.sberbank.transport.oto.cargo.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum CriticalExpireDateEnum {
    LESS_THAN_20("Менее 20%"),
    BETWEEN_20_AND_50("20%-50%"),
    MORE_THAN_50("Более 50%");

    private final String description;
}
