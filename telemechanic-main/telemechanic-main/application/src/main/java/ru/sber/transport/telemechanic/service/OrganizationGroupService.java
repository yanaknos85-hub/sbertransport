package ru.sber.transport.telemechanic.service;

import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

/**
 * Сервис по работе с группами организаций
 */
public interface OrganizationGroupService {
    void save(OrganizationMessage.OrganizationGroup organizationGroup);
}
