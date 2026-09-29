package ru.sber.transport.authentication.providers.auditor.model;

public enum Result {
    WRONG_PASSWORD,
    SUCCESS,
    WRONG_LOGIN,
    TOO_MANY_LOGIN_TRIES
}
