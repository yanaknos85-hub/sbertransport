package ru.sberbank.ditsib.transport.vehicle.dto;

import java.util.UUID;

public record OrganizationNameWithDepartmentInfo(
        UUID organizationId,
        String organizationName,
        UUID departmentId,
        String departmentName,
        UUID parentId
) {
}
