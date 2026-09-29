package ru.sber.transport.notifications.database.dao.messages.corp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.notifications.database.model.coprorate.Department;

import java.util.UUID;

/**
 * Department repository
 */
@Repository
@Transactional(readOnly = true)
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
}
