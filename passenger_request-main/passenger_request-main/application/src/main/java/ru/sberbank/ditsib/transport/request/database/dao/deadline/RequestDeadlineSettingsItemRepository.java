package ru.sberbank.ditsib.transport.request.database.dao.deadline;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.deadline.RequestDeadlineSettingsItem;

import java.util.UUID;

public interface RequestDeadlineSettingsItemRepository extends JpaRepository<RequestDeadlineSettingsItem, UUID> {
}
