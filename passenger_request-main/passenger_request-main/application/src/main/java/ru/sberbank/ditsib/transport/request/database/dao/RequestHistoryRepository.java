package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.RequestHistoryElement;

import java.util.UUID;

/**
 * Repository for requestHistoryElements
 */
@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistoryElement, UUID> {

}
