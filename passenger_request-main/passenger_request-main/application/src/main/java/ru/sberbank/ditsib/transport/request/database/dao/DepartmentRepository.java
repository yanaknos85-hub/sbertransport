package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;

import java.util.*;

/**
 * Repository of department
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    /**
     * Получить список подразделений по флагу активности
     *
     * @param isActive флаг активности
     *
     * @return список подразделений
     */
    List<Department> findAllByActive(boolean isActive);
    
    /**
     * Получить подразделение с согласующими
     */
    @Query("select d from Department d left join fetch d.approvers where d.id = :departmentId")
    Optional<Department> getDepartmentWithApprovers(UUID departmentId);
    
    /**
     * Получить подразделения с согласующими
     */
    @Query("select d from Department d left join fetch d.approvers where d.id in (:departmentIds)")
    Set<Department> getDepartmentsWithApprovers(Collection<UUID> departmentIds);
    
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "truncate table request.department CASCADE")
    void clearAll();
    
}
