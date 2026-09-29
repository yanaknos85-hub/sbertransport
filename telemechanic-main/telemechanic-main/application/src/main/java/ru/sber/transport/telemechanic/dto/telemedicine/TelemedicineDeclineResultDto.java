package ru.sber.transport.telemechanic.dto.telemedicine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TelemedicineDeclineResultDto(
        
        int systPressure,
        int dyastPressure,
        int pulse,
        BigDecimal temperature,
        BigDecimal alcohol,
        String comment,
        LocalDateTime decisionTime
) {
}
