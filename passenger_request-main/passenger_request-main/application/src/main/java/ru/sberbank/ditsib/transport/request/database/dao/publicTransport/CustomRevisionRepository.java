package ru.sberbank.ditsib.transport.request.database.dao.publicTransport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CustomRevision;

public interface CustomRevisionRepository extends JpaRepository<CustomRevision, Long> {
    
    @Transactional
    @Modifying
    @Query(nativeQuery = true,value = "truncate table request_audit.request_for_public cascade")
    void clearPublic();
}
