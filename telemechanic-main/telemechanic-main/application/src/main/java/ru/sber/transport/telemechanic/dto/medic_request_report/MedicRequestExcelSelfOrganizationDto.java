package ru.sber.transport.telemechanic.dto.medic_request_report;

import java.util.Map;

public record MedicRequestExcelSelfOrganizationDto(
        Map<String, String> registry
) {
}
