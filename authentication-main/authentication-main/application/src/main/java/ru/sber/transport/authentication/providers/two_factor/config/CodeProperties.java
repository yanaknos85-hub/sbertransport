package ru.sber.transport.authentication.providers.two_factor.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Настройки кода второго фактора.
 */
@Getter
@Setter
public class CodeProperties {

    /**
     * Длина кода.
     */
    private int length = 4;

    /**
     * Используемые символы.
     */
    private char[] chars = new char[]{'0','1','2','3','4','5','6','7','8','9'};

    /**
     * Путь к методу авторизации по второму фактору.
     */
    private String url = "http://localhost/api/auth/";

}
