package ru.sber.transport.authsb.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.authsb.exceptions.HashException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Тест для утилитного класса Utils.
 */
class UtilsTest {

    @Test
    @DisplayName("Проверка хеширования SHA-256")
    void test1() {

        String input = "Hello, Sber!";
        String hash = HashUtils.hashSha256(input);
        assertThat(hash)
                .isNotNull()
                .hasSize(64) // SHA-256 — 256 бит = 32 байта = 64 hex-символа
                .isEqualTo("7682629e49fb820c7f9f1723bdf6674425ebc3418c5b47b3ea6942c74c09816d");
    }

    @Test
    @DisplayName("Проверка хеширования пустой строки")
    void test2() {
        String input = "";
        String hash = HashUtils.hashSha256(input);
        assertThat(hash)
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    @DisplayName("Проверка хеширования null")
    void test3() {
        assertThatThrownBy(() -> HashUtils.hashSha256(null))
                .isInstanceOf(HashException.class);
    }

    @Test
    @DisplayName("Проверка генерации случайной строки")
    void test4() {
        String uuid = HashUtils.generateRandomString();
        assertThat(uuid).matches(
                "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"
        );
    }

    @Test
    @DisplayName("Проверка генерации случайной строки на уникальность")
    void test5() {
        String first = HashUtils.generateRandomString();
        String second = HashUtils.generateRandomString();
        assertThat(first).isNotEqualTo(second);
    }
}