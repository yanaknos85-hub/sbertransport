package ru.sberbank.ditsib.transport.request.database.dao.deadline;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.deadline.CarsharingJoinDeadlineSettingsItem;

import java.util.UUID;

public interface CarsharingJoinDeadlineSettingsItemRepository
        extends JpaRepository<CarsharingJoinDeadlineSettingsItem, UUID> {
}
