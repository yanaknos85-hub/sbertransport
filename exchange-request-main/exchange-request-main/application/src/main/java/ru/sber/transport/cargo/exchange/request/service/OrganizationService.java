package ru.sber.transport.cargo.exchange.request.service;

import ru.sber.transport.cargo.exchange.request.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с организациями.
 */
public interface OrganizationService {

    /**
     * Находит организацию по её уникальному идентификатору.
     *
     * @param id идентификатор организации
     * @return найденная организация или пустой Optional, если не найдена
     */
    Optional<Organization> findById(UUID id);

    /**
     * Находит организацию по ИНН.
     *
     * @param inn ИНН организации
     * @return найденная организация или пустой Optional, если не найдена
     */
    Optional<Organization> findByInn(String inn);

    /**
     * Сохраняет новую или обновляет существующую организацию.
     *
     * @param organization объект организации
     * @return сохранённая организация
     */
    Organization save(Organization organization);
}


