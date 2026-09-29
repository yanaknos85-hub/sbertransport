package ru.sberbank.ditsib.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.database.model.Employee;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с пользователями.
 * Предоставляет базовые операции CRUD для сущности User.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

   Optional<Employee> findByPersonnelNumber(String personnelNumber);
}
