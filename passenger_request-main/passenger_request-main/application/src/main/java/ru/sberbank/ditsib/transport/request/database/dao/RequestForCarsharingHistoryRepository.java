package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.RequestHistoryElementForCarsharing;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestForCarsharingHistoryRepository extends JpaRepository<RequestHistoryElementForCarsharing, UUID> {
    
    @Query("select history from RequestHistoryElementForCarsharing history where history.requestForCarsharing.id = " +
           ":requestId order by history.changeDate")
    List<RequestHistoryElementForCarsharing> getAllByRequestForCarsharingIdOrderByChangeDate(UUID requestId);
    
}
