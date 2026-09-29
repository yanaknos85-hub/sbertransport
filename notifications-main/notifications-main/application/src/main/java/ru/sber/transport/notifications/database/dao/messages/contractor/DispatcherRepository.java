package ru.sber.transport.notifications.database.dao.messages.contractor;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.contractor.Dispatcher;

import java.util.UUID;

public interface DispatcherRepository extends JpaRepository<Dispatcher, UUID> {
    
}
