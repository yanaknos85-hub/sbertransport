package ru.sber.transport.telemechanic.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Timestamp;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimestampToMillisConverterTest {
    
    private final TimestampToMillisConverter converter = new TimestampToMillisConverter();
    
    @ParameterizedTest
    @MethodSource("sources")
    void from(Timestamp timestamp, Long expected) {
        var actual = converter.from(timestamp);
        assertEquals(expected, actual);
    }
    
    @ParameterizedTest
    @MethodSource("sources")
    void to(Timestamp expected, Long millis) {
        var actual = converter.to(millis);
        assertEquals(expected, actual);
    }
    
    @Test
    void fromType() {
        assertEquals(Timestamp.class, converter.fromType());
    }
    
    @Test
    void toType() {
        assertEquals(Long.class, converter.toType());
    }
    
    static Stream<Arguments> sources() {
        return Stream.of(
                Arguments.of(Timestamp.valueOf("2000-01-01 00:00:00"), 946684800000L),
                Arguments.of(null, null)
                        );
    }
}
