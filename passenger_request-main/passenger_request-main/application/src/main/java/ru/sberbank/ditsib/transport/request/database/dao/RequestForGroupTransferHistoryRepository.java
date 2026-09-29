package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.RequestHistoryElementForGroupTransfer;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestForGroupTransferHistoryRepository extends JpaRepository<RequestHistoryElementForGroupTransfer, UUID> {
    
    List<RequestHistoryElementForGroupTransfer> findAllByRequestForGroupTransferIdOrderByChangeDate(UUID id);
}
