package ru.sber.transport.telemechanic.service.grpc.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.service.grpc.Departments;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class DepartmentsImpl implements Departments {
    
    private final DepartmentsGrpc.DepartmentsBlockingStub departmentsBlockingStub;
    
    @Override
    public Department one(UUID id) {
        final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
        try {
            log.info("Department {} not found. Requesting from source", id);
            final var response = departmentsBlockingStub.one(request);
            log.info("Department {} responded", id);
            return new Department(
                    UUID.fromString(response.getId()),
                    response.getHumanReadableId(),
                    response.getSyncId().getValue(),
                    Organization.builder()
                                .id(UUID.fromString(response.getOrganizationId()))
                                .build(),
                    UUID.fromString(response.getParentId().getValue()),
                    response.getName(),
                    !response.getDeleted(),
                    null,
                    null
            );
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            log.info("Failed to receive department, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }
}
