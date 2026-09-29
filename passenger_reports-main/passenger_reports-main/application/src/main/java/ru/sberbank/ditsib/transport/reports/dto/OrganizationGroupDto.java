package ru.sberbank.ditsib.transport.reports.dto;

import java.util.UUID;

public record OrganizationGroupDto(
        UUID id,
        String name,
        boolean internal
) {
}
