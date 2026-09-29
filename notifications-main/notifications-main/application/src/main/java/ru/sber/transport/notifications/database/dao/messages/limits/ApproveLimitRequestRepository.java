package ru.sber.transport.notifications.database.dao.messages.limits;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.limits.ApproveLimitRequest;

import java.util.UUID;

public interface ApproveLimitRequestRepository extends JpaRepository<ApproveLimitRequest, UUID> {
}
