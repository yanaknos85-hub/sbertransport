package ru.sberbank.ditsib.transport.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Тип разъездного характера сотрудника
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ItinerantType {

    /**
     * не разъездной.
     */
    NONE("Не разъездной"),

    /**
     * Разъездной.
     */
    FULL("Разъездной"),

    /**
     * Гибридный.
     */
    PARTIAL("Частично разъездной");


    /**
     * Получение типа по названию.
     *
     * @param name название.
     *
     * @return тип.
     */
    public static Optional<ItinerantType> getByName(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }

        for (ItinerantType value : ItinerantType.values()) {
            if (value.name().equals(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
    
    private final String description;

}
