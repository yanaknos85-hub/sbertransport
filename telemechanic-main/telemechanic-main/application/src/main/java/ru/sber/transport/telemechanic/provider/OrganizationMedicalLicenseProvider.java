package ru.sber.transport.telemechanic.provider;

import ru.sber.transport.telemechanic.messaging.listener.message.OrganizationMedicalLicenseMessage;

/**
 * Поставщик данных по медицинским лицензиям организаций
 */
public interface OrganizationMedicalLicenseProvider {
    
    /**
     * Сохранение
     *
     * @param message {@link OrganizationMedicalLicenseMessage}
     */
    void save(OrganizationMedicalLicenseMessage message);
    
}
