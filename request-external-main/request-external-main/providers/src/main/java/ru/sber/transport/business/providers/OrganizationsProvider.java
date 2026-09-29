package ru.sber.transport.business.providers;

import java.util.UUID;
import ru.sber.transport.request.external.model.Organization;

/**
 * Провайдер работы с организациями
 */
public interface OrganizationsProvider {

    /**
     * Сохраняет организацию
     *
     * @param source организация для сохранения
     * @return сохраненная организация
     */
    Organization save(Organization source);

    /**
     * Получает организацию по идентификатору
     *
     * @param id идентификатор организации
     * @return организация
     */
    Organization get(UUID id);
}
