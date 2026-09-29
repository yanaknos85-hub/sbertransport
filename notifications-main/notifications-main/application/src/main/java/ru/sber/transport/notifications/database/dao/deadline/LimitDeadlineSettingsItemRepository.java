package ru.sber.transport.notifications.database.dao.deadline;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.deadline.LimitDeadlineSettingsItem;

import java.util.UUID;

public interface LimitDeadlineSettingsItemRepository extends JpaRepository<LimitDeadlineSettingsItem, UUID> {
}
