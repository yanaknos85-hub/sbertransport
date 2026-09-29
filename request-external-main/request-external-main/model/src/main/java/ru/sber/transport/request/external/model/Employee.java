package ru.sber.transport.request.external.model;

import java.util.UUID;

/**
 * Интерфейс сотрудников
 */
public interface Employee {

    /**
     * Идентификатор сотрудника
     *
     * @return идентификатор сотрудника
     */
    UUID getId();

    /**
     * Идентификатор подразделения, которому принадлежит сотрудник
     *
     * @return идентификатор подразделения
     */
    UUID getDepartmentId();

    /**
     * Идентификатор организации, которой принадлежит сотрудник
     *
     * @return идентификатор организации
     */
    UUID getOrganizationId();

    /**
     * Фамилия сотрудника
     *
     * @return фамилия сотрудника
     */
    String getLastName();

    /**
     * Имя сотрудника
     *
     * @return имя сотрудника
     */
    String getFirstName();

    /**
     * Отчество сотрудника
     *
     * @return отчество сотрудника
     */
    String getPatronymic();

    /**
     * Идентификатор должности сотрудника
     *
     * @return идентификатор должности сотрудника
     */
    UUID getPositionId();

    /**
     * Табельный номер сотрудника
     *
     * @return табельный номер сотрудника
     */
    String getPersonnelNumber();

    /**
     * Место возникновения затрат (МВЗ)
     *
     * @return Место возникновения затрат (МВЗ)
     */
    String getCostCenter();

}
