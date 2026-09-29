package ru.sber.transport.request.external.providers.grpc;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.request.external.model.Organization;

@Slf4j
@RequiredArgsConstructor
public class OrganizationsProviderGrpcProvaderImpl implements OrganizationsProvider {

    private final OrganizationsGrpc.OrganizationsBlockingStub stub;

    @Override
    public Organization save(Organization source) {
        throw new UnsupportedOperationException("gRPC saving is not implemented");
    }

    @Override
    public Organization get(UUID id) {
        try {
            log.info("Organization {} not found. Requesting from source", id);
            final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
            final var response = stub.one(request);
            log.info("Organization {} responded", id);
            return new Organization() {

                @Override
                public UUID getId() {
                    return UUID.fromString(response.getId());
                }

                @Override
                public long getDigitId() {
                    return response.getDigitId();
                }

                @Override
                public List<String> getAvailableClasses() {
                    return List.of();
                }
            };
        } catch (Exception e) {
            log.warn("Failed to receive organization, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }
}