package ru.sber.transport.request.external.model.overrun;

/**
 * Результат проверки превышения лимита километража
 *
 * @param comment Комментарий из ответа сервиса check
 * @param totalDistance Суммарный километраж из ответа сервиса check
 */
public record OverrunCheckResult(String comment, int totalDistance) {
}
