package ru.sber.transport.trips.cargo.messaging;

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
