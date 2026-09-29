package ru.sber.transport.etrn.dto;

/**
 * DTO-представление одной проверки внутри Verifications.
 */
public record CheckEntryDto(
        String name,
        Boolean passed
) {}
