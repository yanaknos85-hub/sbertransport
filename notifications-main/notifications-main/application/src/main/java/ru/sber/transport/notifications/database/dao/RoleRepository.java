package ru.sber.transport.notifications.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.settings.restriction.Role;

/**
 * Репозиторий для работы с ролями.
 */
public interface RoleRepository extends JpaRepository<Role, String> {
}