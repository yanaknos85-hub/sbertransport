package ru.sber.transport.business.providers;

import java.util.UUID;

/**
 * Провайдер информации о доступных классах обслуживания.
 */
public interface AvailableClasses {

    /**
     * Проверяет, есть ли у организации доступный класс обслуживания сотрудника с указанным должностью
     *
     * @param organizationId идентификатор организации
     * @param positionId     идентификатор должности
     * @param departmentId   идентификатор подразделения
     * @return true, если класс обслуживания доступен
     */
    boolean exists(UUID organizationId, UUID positionId, @Deprecated UUID departmentId);

    /**
     * Проверяет, есть ли у пользователя доступный класс обслуживания сотрудника с указанным должностью
     *
     * @param user идентификатор пользователя
     * @return true, если класс обслуживания доступен
     * @deprecated Только на MVP
     */
    @Deprecated(since = "4.9.0")
    boolean allow(UUID user);
}
