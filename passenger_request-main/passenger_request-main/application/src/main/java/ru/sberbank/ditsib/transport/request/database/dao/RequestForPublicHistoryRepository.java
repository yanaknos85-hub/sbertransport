package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.RequestHistoryElementForPublic;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestForPublicHistoryRepository extends JpaRepository<RequestHistoryElementForPublic, UUID> {
    
    @Query("select history from RequestHistoryElementForPublic history where history.requestForPublic.id = " +
           ":requestId order by history.changeDate")
    List<RequestHistoryElementForPublic> getAllByRequestForPublicIdOrderByChangeDate(UUID requestId);
    
}
