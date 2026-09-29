package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.RequestHistoryElementForPersonal;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestForPersonalHistoryRepository  extends JpaRepository<RequestHistoryElementForPersonal, UUID> {
    
    @Query("select history from RequestHistoryElementForPersonal history where history.requestForPersonal.id = " +
           ":requestId order by history.changeDate")
    List<RequestHistoryElementForPersonal> getAllByRequestForPersonalIdOrderByChangeDate(UUID requestId);
    
}
