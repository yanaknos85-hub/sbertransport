package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.reports.model.InboxMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InboxMessageRepository extends JpaRepository<InboxMessage, UUID> {
    List<InboxMessage> findAllByStatus(String status);
    
    @Modifying
    @Transactional
    @Query("Delete from InboxMessage im where im.receivedAt < :dateTime")
    int deleteAllOlderThan(@Param("dateTime") LocalDateTime dateTime);
}
