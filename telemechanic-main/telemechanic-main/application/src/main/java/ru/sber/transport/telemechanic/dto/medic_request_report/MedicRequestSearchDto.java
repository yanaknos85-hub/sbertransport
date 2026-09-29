package ru.sber.transport.telemechanic.dto.medic_request_report;

import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.enumerate.MedicRequestField;

import java.util.Set;
import java.util.UUID;

public record MedicRequestSearchDto(
        Set<MedicRequestField> fieldSet,
        String searchText,
        String personnelNumber,
        String humanReadableId,
        UUID organizationId,
        Set<UUID> departmentIdSet,
        DateRange period
) {
}
