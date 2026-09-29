package ru.sber.transport.request.external.messaging;

import java.util.UUID;
import ru.sber.transport.request.external.model.Employee;

public record TestEmployee(
        UUID id,
        String firstName,
        String lastName,
        String patronymic,
        UUID departmentId,
        UUID organizationId,
        UUID positionId,
        String personnelNumber,
        String costCenter
) implements Employee {
    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getDepartmentId() {
        return departmentId;
    }

    @Override
    public UUID getOrganizationId() {
        return organizationId;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getPatronymic() {
        return patronymic;
    }

    @Override
    public UUID getPositionId() {
        return positionId;
    }

    @Override
    public String getPersonnelNumber() {
        return personnelNumber;
    }

    @Override
    public String getCostCenter() {
        return costCenter;
    }
}
