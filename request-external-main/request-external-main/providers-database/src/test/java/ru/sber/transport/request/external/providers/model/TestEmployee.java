package ru.sber.transport.request.external.providers.model;

import java.util.UUID;
import ru.sber.transport.database.external_request.tables.records.EmployeeRecord;
import ru.sber.transport.request.external.model.Employee;

public record TestEmployee(UUID getId, UUID getDepartmentId, UUID getPositionId, UUID getOrganizationId,
                           String getLastName, String getFirstName, String getPatronymic, String getPersonnelNumber,
                           String getCostCenter) implements Employee {

    public TestEmployee(EmployeeRecord source) {
        this(
                source.getId(),
                source.getDepartmentId(),
                source.getPositionId(),
                source.getOrganizationId(),
                source.getLastName(),
                source.getFirstName(),
                source.getPatronymic(),
                source.getPersonnelNumber(),
                source.getCostCenter());
    }

}