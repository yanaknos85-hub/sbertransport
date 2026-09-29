package ru.sber.transport.request.external.model;

import java.util.UUID;

public record TestEmployee(UUID getId, String getFirstName, String getLastName, String getPatronymic,
                           UUID getDepartmentId, UUID getOrganizationId, UUID getPositionId,
                           String getPersonnelNumber, String getCostCenter) implements Employee {
}
