package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.RequestHistoryElementForTaxi;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestForTaxiHistoryRepository extends JpaRepository<RequestHistoryElementForTaxi, UUID> {
    
    @Query("select history from RequestHistoryElementForTaxi history where history.requestForTaxi.id = " +
           ":requestId order by history.changeDate")
    List<RequestHistoryElementForTaxi> getAllByRequestForTaxiIdOrderByChangeDate(UUID requestId);
    
}
