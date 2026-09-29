package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

/**
 * Вид транспортной услуги
 */
@Getter
@RequiredArgsConstructor
public enum TransportServiceType {

    /**
     * Перевозка сотрудников.
     */
    EMPLOYEE_TRANSPORTATION("Перевозка сотрудников"),

    /**
     * Перевозка грузов.
     */
    CARGO_TRANSPORTATION("Грузоперевозки"),

    /**
     * Ремонт.
     */
    REPAIR("Ремонт");

    /**
     * Получение типа транспорта по описанию.
     *
     * @param description описание.
     *
     * @return тип транспорта.
     */
    public static TransportServiceType valueOfDescription(String description) {
        for (var item : TransportServiceType.values()) {
            if (item.getDescription().equalsIgnoreCase(description)) {
                return item;
            }
        }
        throw new NoSuchElementException(String.format("There is no value for description '%s'", description));
    }
    
    private final String description;
}
