package ru.sber.transport.notifications.database.model.request;

/**
 * Сообщение с данными водителя
 *
 * @param lastName фамилия.
 * @param firstName имя.
 * @param patronymic отчество.
 * @param phone номер телефона.
 */
public record Driver(
        String lastName,
        String firstName,
        String patronymic,
        String phone
) {
}
