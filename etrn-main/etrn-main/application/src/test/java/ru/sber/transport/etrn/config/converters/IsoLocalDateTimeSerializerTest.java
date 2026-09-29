package ru.sber.transport.etrn.config.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IsoLocalDateTimeSerializerTest {

    private IsoLocalDateTimeSerializer serializer;

    @Mock
    private JsonGenerator generator;

    @Mock
    private SerializerProvider serializerProvider;

    @BeforeEach
    void setUp() {
        serializer = new IsoLocalDateTimeSerializer();
    }

    @Test
    @DisplayName("convert — преобразование LocalDateTime в ISO-строку")
    void convert_shouldReturnIsoString() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 10, 30, 45);
        String result = serializer.convert(dateTime);

        assertThat(result).isEqualTo("2025-01-15T10:30:45");
    }

    @Test
    @DisplayName("convert — преобразование с миллисекундами")
    void convert_withMillis_shouldIncludeFraction() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 10, 30, 45, 123_000_000);
        String result = serializer.convert(dateTime);

        assertThat(result).isEqualTo("2025-01-15T10:30:45.123");
    }

    @Test
    @DisplayName("serialize — запись ISO-строки в JsonGenerator")
    void serialize_shouldWriteIsoString() throws IOException {
        LocalDateTime dateTime = LocalDateTime.of(2025, 6, 1, 14, 0, 0);

        serializer.serialize(dateTime, generator, serializerProvider);

        verify(generator).writeString("2025-06-01T14:00:00");
    }

    @Test
    @DisplayName("serialize — пустая LocalDateTime")
    void serialize_epochTime() throws IOException {
        LocalDateTime dateTime = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

        serializer.serialize(dateTime, generator, serializerProvider);

        verify(generator).writeString("1970-01-01T00:00:00");
    }
}
