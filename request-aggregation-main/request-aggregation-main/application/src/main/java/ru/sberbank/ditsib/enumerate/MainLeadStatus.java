package ru.sberbank.ditsib.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MainLeadStatus {

    GENERATING("Формируется"),
    GENERATED("Сформирован"),
    IN_REVIEW("На согласовании"),
    CANCELED("Отменено на любой стадии пути");

    private final String description;
}
