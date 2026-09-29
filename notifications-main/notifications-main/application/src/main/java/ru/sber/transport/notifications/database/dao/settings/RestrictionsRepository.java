package ru.sber.transport.notifications.database.dao.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionSettings;

import java.util.UUID;

/**
 * Репозитории для работы с настройками ограничений.
 */
public interface RestrictionsRepository extends JpaRepository<RestrictionSettings, UUID> {
}
