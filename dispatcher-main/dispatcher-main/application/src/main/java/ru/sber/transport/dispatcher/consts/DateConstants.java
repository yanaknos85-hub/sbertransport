package ru.sber.transport.dispatcher.consts;

import java.time.format.DateTimeFormatter;

public final class DateConstants {

    public static final String DDMMYYYY = "dd.MM.yyyy";
    public static final DateTimeFormatter DDMMYYYY_FORMATTER = DateTimeFormatter.ofPattern(DDMMYYYY);

    private DateConstants() {}
}
