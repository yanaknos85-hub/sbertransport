package ru.sber.transport.notifications.database.dao.messages.contractor;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.contractor.Contractor;

import java.util.UUID;

public interface ContractorRepository extends JpaRepository<Contractor, UUID> {
}
