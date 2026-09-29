package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.enumerate.FileStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest
@DisplayName("Тест репозитория CheckPhotoRepository")
@Sql(scripts = {
        "/scripts/cleanup_database.sql",
        "/scripts/basic_corp_structure.sql",
        "/scripts/transport.sql",
        "/scripts/request_with_checks.sql",
        "/scripts/check_photo.sql"
})
class CheckPhotoRepositoryTest {
    
    @Autowired
    private CheckPhotoRepository checkPhotoRepository;
    
    @Test
    void countAllUploadedByCreationTime() {
        var thresholdDateTime = LocalDateTime.now().minusDays(1);
        assertThat(checkPhotoRepository.countAllUploadedByCreationTime(thresholdDateTime)).isEqualTo(3);
    }
    
    @Test
    void findAllUploadedIdByCreationTime() {
        var thresholdDateTime = LocalDateTime.now().minusDays(1);
        assertThat(checkPhotoRepository.findAllUploadedIdByCreationTime(thresholdDateTime, 10))
                .isEqualTo(List.of(UUID.fromString("78576f73-c7a5-4ea9-a893-b84917361ec1"),
                                   UUID.fromString("0bd93c2e-9c27-4de1-88c5-c59f6ab9dfd6"),
                                   UUID.fromString("e4d08a43-55d5-4458-8a6b-88a1ad088f1b")));
    }
    
    @Test
    void updateStatusesForBatchOfPhotos() {
        var photoIds = List.of(UUID.fromString("78576f73-c7a5-4ea9-a893-b84917361ec1"),
                               UUID.fromString("0bd93c2e-9c27-4de1-88c5-c59f6ab9dfd6"),
                               UUID.fromString("e4d08a43-55d5-4458-8a6b-88a1ad088f1b"));
        checkPhotoRepository.updateStatusesForBatchOfPhotos(photoIds);
        var checkPhotoList = checkPhotoRepository.findAll();
        assertThat(checkPhotoList).isNotEmpty()
                                  .filteredOn(i -> photoIds.contains(i.getCheckId()))
                                  .allSatisfy(i -> assertThat(i.getFileStatus()).isEqualTo(FileStatus.DELETED));
    }
}
