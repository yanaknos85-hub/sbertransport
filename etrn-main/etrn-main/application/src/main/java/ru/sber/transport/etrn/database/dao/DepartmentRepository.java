package ru.sber.transport.etrn.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.etrn.database.model.Department;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    /**
     * Получить список подразделений по флагу активности
     *
     * @param isActive флаг активности
     * @return список подразделений
     */
    List<Department> findAllByActive(boolean isActive);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "truncate table etrn_cargo.department CASCADE")
    void clearAll();
}