package ru.sberbank.ditsib.transport.constants.cargo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Статусы импорта грузов.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum CargoImportStatus {

    /**
     * Импорт завершен.
     */
    SUCCESS("Успех"),

    /**
     * Импорт еще в процессе.
     */
    IN_PROGRESS("В процессе"),

    /**
     * Импорт завершен с ошибкой.
     */
    ERROR("Не успех");
    
    private final String description;
}
