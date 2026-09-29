package ru.sberbank.ditsib.transport.request.database.dao.publicTransport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompensationDocumentRepository extends JpaRepository<CompensationDocument, UUID> {
   
    List<CompensationDocument> findAllByFolder(UUID folder);
    
    Optional<CompensationDocument> findByFileName(String fileName);
    
    Optional<CompensationDocument> findByFileNameAndFolder(String fileName, UUID folder);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE from CompensationDocument")
    void deleteAll();
    
}
