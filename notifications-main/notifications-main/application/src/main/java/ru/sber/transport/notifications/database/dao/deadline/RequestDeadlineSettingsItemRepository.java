package ru.sber.transport.notifications.database.dao.deadline;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.deadline.RequestDeadlineSettingsItem;

import java.util.UUID;

public interface RequestDeadlineSettingsItemRepository extends JpaRepository<RequestDeadlineSettingsItem, UUID> {
}
