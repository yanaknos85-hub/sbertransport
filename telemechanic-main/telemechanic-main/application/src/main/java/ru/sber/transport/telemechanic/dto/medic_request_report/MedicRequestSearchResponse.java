package ru.sber.transport.telemechanic.dto.medic_request_report;

import java.util.List;
import java.util.Map;

public record MedicRequestSearchResponse(
        List<Map<String, Object>> content,
        int totalElements
) {
}
