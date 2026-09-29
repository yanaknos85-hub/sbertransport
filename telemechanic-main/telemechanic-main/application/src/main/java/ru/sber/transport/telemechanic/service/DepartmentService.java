package ru.sber.transport.telemechanic.service;


import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.dto.DepartmentDto;
import ru.sber.transport.telemechanic.dto.OrganizationWithAutoparkDto;
import ru.sber.transport.telemechanic.dto.TariffDepartmentResponse;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service for working with departments.
 */
public interface DepartmentService {
    
    /**
     * Get department by ID.
     *
     * @param id ID of department.
     *
     * @return department.
     */
    Optional<Department> get(UUID id);
    
    /**
     * Delete department.
     *
     * @param entity department to delete.
     */
    void delete(Department entity);
    
    /**
     * Save department.
     *
     * @param entity department to save.
     */
    Department save(Department entity);
    
    List<DepartmentDto> getByOrganizationId(UUID organizationId);
    
    /**
     * Получить ID подразделений, которые не относятся к организации
     * @param organizationId
     * @param departmentIdSet
     * @return
     */
    Set<UUID> getNotOrganizationIds(UUID organizationId, Set<UUID> departmentIdSet);
    
    /**
     * Получить список подразделений по идентификатор организации, у которых есть активные тарифы
     * (в списке присутсвуют потомки выбранных подразделений)
     * @param organizationId идентификатор организации
     * @return {@link TariffDepartmentResponse} список подразделений
     */
    List<TariffDepartmentResponse> getAllWithActiveTariff(UUID organizationId);
    
    /**
     * Сохраняем подразделение, которую мы получим по grpc из сервиса corporate
     *
     * @param message Сообщение в случае ошибки
     * @param id Идентификатор записи о подразделении
     */
    void saveGrpcEntity(String message, UUID id) throws AwaitingSynchronizationException;
    
    /**
     * Сохраняем родительские подразделения, которым мы получим по grpc из сервиса corporate
     * @param parentId  Идентификатор записи родителя в таблице department
     */
    void saveGrpcParentEntities(UUID parentId) throws AwaitingSynchronizationException;
    
    /**
     * Получение подразделения по идентификатору филиала контрагента
     * @param id идентификатор филила контрагента
     * @return подразеление
     */
    Optional<Department> getByAutoparkId(UUID id);
    
    /**
     * Получаем организацию с филиалами внутреннего автопарка
     * @param organizationId идентификатор организаци
     * @return организация
     */
    OrganizationWithAutoparkDto getAllWithInternalAutoPark(UUID organizationId);
    
    /**
     * Получнение идентификаторов подразделений по идентификаторам филиалов контрагента
     * @param autoparkIds список идентификаторов филиалов контрагента
     * @return список идентификаторов подразделений
     */
    List<UUID> getDepartmentIdsByAutoparkIds(List<UUID> autoparkIds);
    
    
}
