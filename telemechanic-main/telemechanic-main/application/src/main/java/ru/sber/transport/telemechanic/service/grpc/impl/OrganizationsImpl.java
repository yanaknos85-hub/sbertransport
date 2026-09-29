package ru.sber.transport.telemechanic.service.grpc.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.telemechanic.database.model.Contact;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.enumerate.ContactType;
import ru.sber.transport.telemechanic.service.grpc.Organizations;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class OrganizationsImpl implements Organizations {
    
    private final OrganizationsGrpc.OrganizationsBlockingStub organizationsBlockingStub;
    
    @Override
    public Organization one(UUID id) {
        final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
        try {
            log.info("Organization {} not found. Requesting from source", id);
            final var response = organizationsBlockingStub.one(request);
            log.info("Organization {} responded", id);
            var contacts = response.getContactsList().stream()
                                   .map(contact -> new Contact(UUID.fromString(contact.getId()),
                                                               ContactType.valueOf(contact.getType().name()),
                                                               contact.getValue()))
                                   .collect(Collectors.toSet());
            return new Organization(
                    UUID.fromString(response.getId()),
                    (long) response.getDigitId(),
                    response.getName(),
                    response.getMsrn(),
                    response.getTid(),
                    response.getGroup().hasNull() ? null : UUID.fromString(response.getGroup().getValue()),
                    !response.getDeleted(),
                    contacts,
                    null,
                    null
            );
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            log.info("Failed to receive organization, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }
}
