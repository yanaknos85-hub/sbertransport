package ru.sber.transport.request.external.providers.grpc;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.request.external.model.Employee;

@Slf4j
@RequiredArgsConstructor
public class EmployeesGrpcProviderImpl implements EmployeesProvider {

    private final EmployeesGrpc.EmployeesBlockingStub stub;

    @Override
    public Employee save(Employee source) {
        throw new UnsupportedOperationException("gRPC does not support this method");
    }

    @Override
    public Employee get(UUID id) {
        final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
        try {
            log.info("Employee {} not found. Requesting from source", id);
            final var response = stub.one(request);
            log.info("Employee {} responded", id);
            return new GrpcEmployee(response);
        } catch (Exception e) {
            log.warn("Failed to receive employee, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }

    private record GrpcEmployee(@Delegate OrganizationsOuterClass.Employee response) implements Employee {

        @Override
        public UUID getDepartmentId() {
            return UUID.fromString(response.getDepartmentId());
        }

        @Override
        public UUID getPositionId() {
            return UUID.fromString(response.getPositionId());
        }

        @Override
        public UUID getOrganizationId() {
            return UUID.fromString(response.getOrganizationId());
        }

        @Override
        public UUID getId() {
            return UUID.fromString(response.getId());
        }

        @Override
        public String getPatronymic() {
            return response.getPatronymic().hasValue() ? response.getPatronymic().getValue() : null;
        }

        @Override
        public String getCostCenter() {
            return response.getCostCenter().hasValue() ? response.getCostCenter().getValue() : null;
        }

    }
}
