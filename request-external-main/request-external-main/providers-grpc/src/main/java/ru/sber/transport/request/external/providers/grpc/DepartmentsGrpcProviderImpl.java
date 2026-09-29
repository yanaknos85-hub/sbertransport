package ru.sber.transport.request.external.providers.grpc;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.request.external.model.Department;
import ru.sber.transport.request.external.model.DepartmentStatus;

@Slf4j
@RequiredArgsConstructor
public class DepartmentsGrpcProviderImpl implements DepartmentsProvider {

    private final DepartmentsGrpc.DepartmentsBlockingStub stub;

    @Override
    public Department save(Department source) {
        throw new UnsupportedOperationException("gRPC does not support this method");
    }

    @Override
    public Department get(UUID id) {
        final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
        try {
            log.info("Department {} not found. Requesting from source", id);
            final var response = stub.one(request);
            log.info("Department {} responded", id);
            return new Department() {
                @Override
                public UUID getId() {
                    return UUID.fromString(response.getId());
                }

                @Override
                public UUID getHeadId() {
                    final var headId = response.getHeadId();
                    if (headId.hasValue()) {
                        return UUID.fromString(headId.getValue());
                    }
                    return null;
                }

                @Override
                public String getName() {
                    return response.getName();
                }

                @Override
                public UUID getParentId() {
                    final var parentId = response.getParentId();
                    if (parentId.hasValue()) {
                        return UUID.fromString(parentId.getValue());
                    }
                    return null;
                }

                @Override
                public DepartmentStatus getStatus() {
                    return response.getDeleted() ? DepartmentStatus.INACTIVE : DepartmentStatus.ACTIVE;
                }

                @Override
                public UUID getOrganizationId() {
                    return UUID.fromString(response.getOrganizationId());
                }

                @Override
                public Integer getLevel() {
                    //тут константа, т.к на стороне corporate нет такого поля. При сохранении на нашей стороне оно высчитывается
                    return 1;
                }
            };
        } catch (Exception e) {
            log.warn("Failed to receive department, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }

    @Override
    public Set<UUID> getChildrenDepartments(Set<UUID> ids) {
        throw new UnsupportedOperationException("gRPC does not support this method");
    }

    @Override
    public List<Department> findDepartmentsByOrganizationIdAndStatusAndLevelBetween(UUID organizationId, DepartmentStatus status, int levelFrom, int levelTo) {
        throw new UnsupportedOperationException("gRPC does not support this method");
    }
}
