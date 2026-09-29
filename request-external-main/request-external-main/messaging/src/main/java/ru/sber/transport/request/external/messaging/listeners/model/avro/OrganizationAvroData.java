package ru.sber.transport.request.external.messaging.listeners.model.avro;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.messages.corporate.avro.OrganizationMessage;
import ru.sber.transport.request.external.model.Organization;

/**
 * Данные организации
 */
@RequiredArgsConstructor
public final class OrganizationAvroData implements Organization {

    @Delegate
    private final OrganizationMessage delegate;

}
