package ru.sber.transport.notifications.database.dao.deadline;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.deadline.DeadlineSettingsItem;

import java.util.UUID;

public interface DeadlineSettingsItemRepository extends JpaRepository<DeadlineSettingsItem, UUID> {
}
