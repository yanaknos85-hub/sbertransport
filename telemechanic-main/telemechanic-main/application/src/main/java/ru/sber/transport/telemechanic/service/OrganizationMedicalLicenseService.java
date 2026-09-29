package ru.sber.transport.telemechanic.service;


import ru.sber.transport.telemechanic.database.model.OrganizationMedicalLicense;

import java.util.UUID;

/**
 * Сервис по работе с медицинскими лицензиями организаций
 */
public interface OrganizationMedicalLicenseService {
    
    /**
     * Создание
     *
     * @param entity {@link OrganizationMedicalLicense}
     */
    OrganizationMedicalLicense save(OrganizationMedicalLicense entity);
    
    /**
     * Проверяем, активна ли медицинская лицензия
     *
     * @param id Идентификатор записи о медицинской лицензии
     *
     * @return наличие активной лицензии
     */
    boolean existsAndActive(UUID id);

    /**
     * Проверка данных о медике и лицензии
     *
     * @param userId Идентификатор записи с таблицы corporate.user
     */
    void checkMedicalLicense(UUID userId);
    
    /**
     * Получаем медицинскую лицензию организации по идентификатору записи о подразделении
     *
     * @param departmentId идентификатор записи о подразделении
     *
     * @return {@link OrganizationMedicalLicense}
     */
    OrganizationMedicalLicense getMedicalLicense(UUID departmentId);
}