package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.exception.TransportUnboundException;

import java.util.Set;
import java.util.UUID;

public interface TransportValidationService {

    /**
     * Проверяет, что организация диспетчера связана с транспортом.
     *
     * @param organizationIds Идентификатор организаций, связанных с транспортом
     * @param dispatcherOrgId Идентификатор организации диспетчера
     * @param transportId Идентификатор транспортного средства
     * @throws TransportUnboundException если транспортное средство не связано с организацией диспетчера
     */
    void validateTransportOrgsContainsDispatcherOrg(Set<UUID> organizationIds, UUID dispatcherOrgId, UUID transportId);
}
