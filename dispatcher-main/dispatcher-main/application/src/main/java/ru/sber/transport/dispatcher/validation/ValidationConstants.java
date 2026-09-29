package ru.sber.transport.dispatcher.validation;

public final class ValidationConstants {

    private ValidationConstants() {
    }

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_%.+\\-]+(\\.[a-zA-Z0-9_%.+\\-]+)*@[a-zA-ZА-ЯЁа-яё0-9.-]+\\.[a-zA-ZА-ЯЁа-яё]{2,6}$";

}
