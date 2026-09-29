package ru.sber.transport.telemechanic.dto;

/**
 * Данные файла.
 *
 * @param contentType тип файла.
 * @param stream массив байт файла.
 */
public record FileData(
        String contentType,
        byte[] stream
) {
}
