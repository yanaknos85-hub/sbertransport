package ru.sberbank.ditsib.transport.request.database.model;

/**
 * Сообщение с данными водителя
 *
 * @param lastName фамилия.
 * @param firstName имя.
 * @param patronymic отчество.
 * @param phone номер телефона.
 */
public record Driver(
        String name,
        String secName,
        String patronymic,
        String phone
) {
}
