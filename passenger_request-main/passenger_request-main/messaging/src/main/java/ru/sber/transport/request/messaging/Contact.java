package ru.sber.transport.request.messaging;

import ru.sberbank.ditsib.transport.constants.TaxiStopType;

/**
 * Контакт
 */
public record Contact(String phone, String name, String firstName, String patronymic, TaxiStopType type) {
}
