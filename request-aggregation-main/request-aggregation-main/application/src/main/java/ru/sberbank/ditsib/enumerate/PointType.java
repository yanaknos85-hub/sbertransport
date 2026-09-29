package ru.sberbank.ditsib.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointType {
    START("Начало"),
    END("Конец");

    private final String description;
}
