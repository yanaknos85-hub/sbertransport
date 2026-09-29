package ru.sberbank.ditsib.transport.reports.dao;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.Department;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    
    @Query("SELECT department FROM Request request INNER JOIN request.passenger passenger INNER JOIN passenger.department department " +
           "where request.id in (:requestIds)")
    List<Department> findAllByRequestIdIn(@NonNull Set<UUID> requestIds);
    
    @Query(value = "WITH RECURSIVE r AS (SELECT * FROM reports.department WHERE parent_id = ? UNION SELECT reports.department.* FROM reports" +
                   ".department JOIN r ON reports.department.parent_id = r.id) SELECT * FROM r;", nativeQuery = true)
    List<Department> findAllByParentId(UUID parentId);
    
    List<Department> findByDepartmentName(String departmentName);
    
    
    /**
     * Поиск подразделений по идентификатору организации, наименованиям подразделений, идентификаторам подразделений. Поиск на 1 уровень вглубь
     * @param organizationId идентификатор организации
     * @param departmentNames наименования подразделений
     * @param parentIds идентификаторы родительских подразделений
     * @return список департаментов
     */
    @Query("select distinct department from Department department where department.organizationId = :organizationId " +
           "and department.departmentName in :departmentNames and department.parentId in :parentIds")
    List<Department> findAllByOrganizationIdAndDepartmentNameInAndParentIdIn(UUID organizationId, List<String> departmentNames,
                                                                       List<UUID> parentIds);
    
    /**
     * Поиск подразделений по идентификатору организации, идентификаторам подразделений. Поиск на 1 уровень вглубь
     * @param organizationId идентификатор организации
     * @param parentIds идентификаторы родительских подразделений
     * @return список департаментов
     */
    @Query("select distinct department from Department department where department.organizationId = :organizationId " +
           "and department.parentId in :parentIds")
    List<Department> findAllByOrganizationIdAndParentIdIn(UUID organizationId, List<UUID> parentIds);
    
  
    List<Department> findAllByOrganizationIdAndDepartmentNameIn(UUID organizationId, List<String> departmentNames);
    
    /**
     * Поиск депаратамента 2го уровня
     * @param organizationId идентификатор организации
     * @return список департаментов
     */
    List<Department> findAllByOrganizationIdAndParentIdIsNull(UUID organizationId);
}
