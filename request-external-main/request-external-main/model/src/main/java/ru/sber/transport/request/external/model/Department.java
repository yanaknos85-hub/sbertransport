package ru.sber.transport.request.external.model;

import java.util.UUID;

/**
 * Интерфейс подразделений
 */
public interface Department {

    /**
     * Идентификатор подразделения
     *
     * @return идентификатор подразделения
     */
    UUID getId();

    /**
     * Идентификатор руководителя
     *
     * @return идентификатор руководителя подразделения
     */
    UUID getHeadId();

    /**
     * Идентификатор родительского подразделения
     * @return идентификатор родительского подразделения
     */
    UUID getParentId();

    /**
     * Название подразделения
     * @return название подразделения
     */
    String getName();

    /**
     * Статус подразделения
     * @return статус подразделения
     */
    DepartmentStatus getStatus();

    /**
     * Идентификатор организации
     * @return индентификатор организации
     */
    UUID getOrganizationId();

    /**
     * Уровень подразделения
     * @return уровень подразделения
     */
    Integer getLevel();

}
