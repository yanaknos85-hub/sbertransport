package ru.sber.transport.telemechanic.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MedicalCheckUpModel(
        String data,
        String signature,
        String name,
        UUID ewbUuid,
        MedicInfo medicInfo,
        ExamInfo exam,
        boolean withExamInfo
) {
    public record MedicInfo(
            String fio,
            String personalNumber,
            String organization,
            String department,
            String position,
            String serialNumber,
            LocalDateTime serialEndDateTime) {
    }
    
    public record ExamInfo(
            boolean medicRequestStatus,
            LocalDateTime creationDateTime,
            LocalDateTime medicDecisionDateTime,
            int systPressure,
            int dyastPressure,
            int pulse,
            BigDecimal temperature,
            BigDecimal alcohol,
            String comment
    ) {
    }
}
