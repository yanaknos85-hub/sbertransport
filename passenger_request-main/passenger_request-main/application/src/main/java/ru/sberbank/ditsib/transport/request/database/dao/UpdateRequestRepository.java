package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.UpdateRequest;

import java.util.UUID;

/**
 * Repository for update request
 */
@Repository
public interface UpdateRequestRepository extends JpaRepository<UpdateRequest, UUID> {
    /**
     * Поиск по id заявки
     * @param requestId
     * @return
     */
    @Query("select e from UpdateRequest e inner join fetch e.request r where r.id = :requestId")
    UpdateRequest findByRequestId(UUID requestId);
    
}
