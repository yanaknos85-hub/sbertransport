package ru.sberbank.ditsib.transport.request.database.dao.deadline;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.deadline.LimitDeadlineSettingsItem;

import java.util.UUID;

public interface LimitDeadlineSettingsItemRepository extends JpaRepository<LimitDeadlineSettingsItem, UUID> {
}
