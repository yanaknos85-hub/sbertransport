package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.model.CheckPhoto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CheckPhotoRepository extends JpaRepository<CheckPhoto, UUID> {
    
    @Query("""
           SELECT count(cp) FROM CheckPhoto cp
           WHERE cp.fileStatus = ru.sber.transport.telemechanic.enumerate.FileStatus.UPLOADED
                      AND cp.creationTime < :thresholdDateTime""")
    int countAllUploadedByCreationTime(LocalDateTime thresholdDateTime);
    
    @Query(value = """
                   SELECT cp.id FROM telemechanic.check_photo cp
                   WHERE cp.file_status = 'UPLOADED'
                              AND cp.creation_time < :thresholdDateTime LIMIT :batchSize""",
           nativeQuery = true)
    List<UUID> findAllUploadedIdByCreationTime(LocalDateTime thresholdDateTime, int batchSize);
    
    @Modifying
    @Transactional
    @Query("UPDATE CheckPhoto p SET p.fileStatus = ru.sber.transport.telemechanic.enumerate.FileStatus.DELETED WHERE p.id IN (:photoIds)")
    void updateStatusesForBatchOfPhotos(List<UUID> photoIds);
}
