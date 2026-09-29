package ru.sberbank.ditsib.transport.request.database.dao.deadline;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.deadline.DeadlineSettingsItem;

import java.util.UUID;

public interface DeadlineSettingsItemRepository extends JpaRepository<DeadlineSettingsItem, UUID> {
}
