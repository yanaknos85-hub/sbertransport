package ru.sber.transport.request.messaging;


import java.util.List;

/**
 * Информация о точке маршрута
 */
public record Waypoint(String name, Double latitude, Double longitude, Integer waitTime, List<Contact> passengers) {
}
