package ru.sber.transport.etrn.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO-представление результата проверок (Verifications).
 */
public record VerificationsDto(
        List<CheckEntryDto> checks,
        Boolean overallPassed,
        LocalDateTime verifiedAt
) {}
