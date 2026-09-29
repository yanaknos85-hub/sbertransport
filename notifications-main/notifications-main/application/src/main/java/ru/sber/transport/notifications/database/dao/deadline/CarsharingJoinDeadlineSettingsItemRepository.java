package ru.sber.transport.notifications.database.dao.deadline;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.deadline.CarsharingJoinDeadlineSettingsItem;

import java.util.UUID;

public interface CarsharingJoinDeadlineSettingsItemRepository
        extends JpaRepository<CarsharingJoinDeadlineSettingsItem, UUID> {
}
