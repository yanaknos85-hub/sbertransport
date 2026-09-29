package ru.sber.transport.authentication.business.dto;

/**
 * Допустимые типы авторизации.
 */
public enum AuthType {

    /**
     * Базовая.
     */
    BASIC,

    /**
     * 2-хфакторная.
     */
    TWO_FA
}
