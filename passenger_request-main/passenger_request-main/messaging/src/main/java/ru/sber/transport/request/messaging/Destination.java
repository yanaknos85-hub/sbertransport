package ru.sber.transport.request.messaging;


/**
 * Конечная точка маршрута
 */
public record Destination(String name, Double latitude, Double longitude, Contact contact) {
}
