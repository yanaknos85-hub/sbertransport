package ru.sber.transport.telemechanic.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SnilsHelperTest {

    static Stream<Arguments> isValidSnils() {
        return Stream.of(
            Arguments.of("112-233-445 95", true),
            Arguments.of("07745742300", true),
            Arguments.of("077-457-423 00", true),
            Arguments.of("001-002-003 18", true),

            Arguments.of("112-233-445 00", false),
            Arguments.of("12345678901", false),
            Arguments.of("111-111-111 11", false),
            Arguments.of("12345", false),
            Arguments.of("abc-def-ghi jk", false)
        );
    }

    @MethodSource
    @ParameterizedTest
    void isValidSnils(String snils, boolean expected) {
        assertThat(SnilsHelper.isValidSnils(snils)).isEqualTo(expected);
    }

}
