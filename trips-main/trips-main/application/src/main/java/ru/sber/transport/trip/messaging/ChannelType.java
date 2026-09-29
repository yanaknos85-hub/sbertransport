package ru.sber.transport.trip.messaging;

/**
 * Типы каналов для отправки данных.
 */
public enum ChannelType {

    /**
     * Только веб-сокет.
     */
    WEB_SOCKET,

    /**
     * Только кафка.
     */
    KAFKA,

    /**
     * Оба.
     */
    BOTH
}
