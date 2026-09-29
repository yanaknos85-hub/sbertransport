package ru.sber.transport.business.providers;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import ru.sber.transport.request.external.model.Department;
import ru.sber.transport.request.external.model.DepartmentStatus;

/**
 * Провайдер данных о подразделениях.
 */
public interface DepartmentsProvider {

    /**
     * Сохраняет данные о подразделении
     *
     * @param source данные о подразделении
     * @return сохраненные данные о подразделении
     */
    Department save(Department source);

    /**
     * Получает данные о подразделении
     *
     * @param id идентификатор подразделения
     * @return данные о подразделении
     */
    Department get(UUID id);

    /**
     * Получает идентификаторы всех подразделений, которые являются потомками заданных
     *
     * @param ids идентификаторы подразделений, которые являются родителями
     * @return идентификаторы всех подразделений, которые являются потомками заданных
     */
    Set<UUID> getChildrenDepartments(Set<UUID> ids);

    /**
     * Получает список подразделений по идентификатору организации, статусу и уровню
     *
     * @param organizationId идентификатор организации
     * @param status статус подразделения
     * @param levelFrom уровень от
     * @param levelTo уровень до
     * @return список подразделений
     */
    List<Department> findDepartmentsByOrganizationIdAndStatusAndLevelBetween(UUID organizationId,
                                                                             DepartmentStatus status,
                                                                             int levelFrom, int levelTo);
}
