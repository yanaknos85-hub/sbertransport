package ru.sber.transport.telemechanic.provider;

import ru.sber.transport.telemechanic.messaging.listener.message.FleetOwnerOrganizationMessage;

/**
 * Поставщик данных организаций владельцев автопарков
 */
public interface FleetOwnerOrganizationProvider {
    
    /**
     * Сохранение
     *
     * @param message {@link FleetOwnerOrganizationMessage}
     */
    void save(FleetOwnerOrganizationMessage message);
    
}
