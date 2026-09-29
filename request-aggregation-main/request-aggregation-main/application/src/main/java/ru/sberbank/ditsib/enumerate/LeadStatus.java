package ru.sberbank.ditsib.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeadStatus {

    PROCESSING("В обработке"),
    PROCESSED("Обработано"),
    CANCELED("Отменено на любой стадии пути");

    private final String description;
}