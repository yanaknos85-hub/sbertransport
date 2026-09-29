package ru.sber.transport.notifications.database.dao.messages.corp;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.coprorate.Delegate;

import java.util.UUID;

public interface DelegateRepository extends JpaRepository<Delegate, UUID> {
}
