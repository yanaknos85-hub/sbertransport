package ru.sber.transport.request.external.providers.model;

import java.util.UUID;
import ru.sber.transport.request.external.model.Department;
import ru.sber.transport.request.external.model.DepartmentStatus;

public record TestDepartment(UUID getId, UUID getHeadId, UUID getParentId, String getName, DepartmentStatus getStatus, UUID getOrganizationId, Integer getLevel) implements Department {
}
