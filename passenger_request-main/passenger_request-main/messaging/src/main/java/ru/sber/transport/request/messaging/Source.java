package ru.sber.transport.request.messaging;

/**
 * Стартовая точка маршрута
 */
public record Source(String name, Double latitude, Double longitude) {
}
