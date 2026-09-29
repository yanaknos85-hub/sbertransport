package ru.sber.transport.telemechanic.dto.medic_request_report;

import java.util.Map;

public record MedicRequestExcelAllOrganizationsDto(
        Map<String, String> registry
) {
}
