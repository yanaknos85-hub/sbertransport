package ru.sber.transport.request.messaging;

/**
 * Информация о пассажире
 *
 * @param id      Порядковый номер субъекта
 * @param name    ФИО
 * @param phone   phone
 * @param comment comment
 */
public record PassengerMessage(
        String id,
        String name,
        String phone,
        String comment
) {
}
