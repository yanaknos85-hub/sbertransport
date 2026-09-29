package ru.sberbank.ditsib.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.database.model.Department;

import java.util.UUID;

/**
 * Repository of department
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
}
