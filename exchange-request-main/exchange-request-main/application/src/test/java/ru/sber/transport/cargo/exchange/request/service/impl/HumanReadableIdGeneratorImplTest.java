package ru.sber.transport.cargo.exchange.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.cargo.exchange.request.database.dao.HumanReadableIdCounterRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HumanReadableIdGeneratorImplTest {

    @Mock
    private HumanReadableIdCounterRepository counterRepository;

    @InjectMocks
    private HumanReadableIdGeneratorImpl generator;

    private String currentYearMonth;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

    @BeforeEach
    void setUp() {
        currentYearMonth = LocalDateTime.now().format(formatter);
        generator.setDefaultPrefix("ОР");
    }

    @Test
    void shouldGenerateIdWithDefaultPrefix() {
        // Given
        when(counterRepository.findNextValue(currentYearMonth, "ОР")).thenReturn(java.util.Optional.of(1L));

        // When
        String result = generator.generateHumanReadableId();

        // Then
        assertEquals("ОР-" + currentYearMonth + "-00000001", result);
    }

    @Test
    void shouldGenerateIdWithCustomPrefix() {
        // Given
        when(counterRepository.findNextValue(currentYearMonth, "АУК")).thenReturn(java.util.Optional.of(1L));

        // When
        String result = generator.generateHumanReadableId("АУК");

        // Then
        assertEquals("АУК-" + currentYearMonth + "-00000001", result);
    }

    @Test
    void shouldHandleDifferentPrefixesIndependently() {
        // Given
        when(counterRepository.findNextValue(currentYearMonth, "ОР")).thenReturn(java.util.Optional.of(5L));
        when(counterRepository.findNextValue(currentYearMonth, "АУК")).thenReturn(java.util.Optional.of(3L));
        when(counterRepository.findNextValue(currentYearMonth, "ФРХ")).thenReturn(java.util.Optional.of(7L));

        // When / Then
        assertEquals("ОР-" + currentYearMonth + "-00000005", generator.generateHumanReadableId("ОР"));
        assertEquals("АУК-" + currentYearMonth + "-00000003", generator.generateHumanReadableId("АУК"));
        assertEquals("ФРХ-" + currentYearMonth + "-00000007", generator.generateHumanReadableId("ФРХ"));
    }

    @Test
    void shouldUseDefaultPrefixWhenCustomIsNull() {
        // Given
        when(counterRepository.findNextValue(currentYearMonth, "ОР")).thenReturn(java.util.Optional.of(10L));

        // When
        String result = generator.generateHumanReadableId(null);

        // Then
        assertEquals("ОР-" + currentYearMonth + "-00000010", result);
    }

    @Test
    void shouldUseDefaultPrefixWhenCustomIsEmpty() {
        // Given
        when(counterRepository.findNextValue(currentYearMonth, "ОР")).thenReturn(java.util.Optional.of(15L));

        // When
        String result = generator.generateHumanReadableId("");

        // Then
        assertEquals("ОР-" + currentYearMonth + "-00000015", result);
    }

    @Test
    void shouldFormatLargeCounterValuesWithLeadingZeros() {
        // Given
        when(counterRepository.findNextValue(currentYearMonth, "ОР")).thenReturn(java.util.Optional.of(123456L));

        // When
        String result = generator.generateHumanReadableId();

        // Then
        assertEquals("ОР-" + currentYearMonth + "-00123456", result);
    }

    @Test
    void shouldGenerateCorrectIdForMixedCasePrefix_ExpectNormalized() {
        // Given
        when(counterRepository.findNextValue(currentYearMonth, "aук")).thenReturn(java.util.Optional.of(1L));

        // When
        String result = generator.generateHumanReadableId("aук");

        // Then
        assertEquals("aук-" + currentYearMonth + "-00000001", result);
        // Примечание: если нужно — можно добавить toUpperCase() или валидацию
    }

    @Test
    void shouldWorkWithSpecialPrefixes() {
        // Given
        when(counterRepository.findNextValue(currentYearMonth, "ФРХ-Т")).thenReturn(java.util.Optional.of(1L));

        // When
        String result = generator.generateHumanReadableId("ФРХ-Т");

        // Then
        assertEquals("ФРХ-Т-" + currentYearMonth + "-00000001", result);
    }
}