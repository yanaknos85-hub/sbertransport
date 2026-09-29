package ru.sber.transport.telemechanic.service.grpc.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.grpc.service.PositionsGrpc;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.database.model.Position;
import ru.sber.transport.telemechanic.service.grpc.Positions;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class PositionsImpl implements Positions {
    
    private final PositionsGrpc.PositionsBlockingStub positionsBlockingStub;
    
    @Override
    public Position one(UUID id) {
        final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
        try {
            log.info("Position {} not found. Requesting from source", id);
            final var response = positionsBlockingStub.one(request);
            log.info("Position {} responded", id);
            return new Position(
                    UUID.fromString(response.getId()),
                    Organization.builder()
                                .id(UUID.fromString(response.getOrganizationId()))
                                .build(),
                    response.getName(),
                    !response.getDeleted()
            );
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            log.info("Failed to receive position, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }
}
