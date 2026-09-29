package ru.sber.transport.telemechanic.scheduler;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.service.CheckPhotoService;
import ru.sber.transport.telemechanic.service.FileService;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;


@Log4j2
@EmbeddedPostgres
@DisplayName("Тесты для CheckPhotoScheduler")
@SpringBootTest(properties = {
        "check-photo.auto-deletion.enabled=true",
        "check-photo.auto-deletion.cron=*/5 * * * * *",
        "check-photo.auto-deletion.lock-at-least-for=PT5S",
        "check-photo.auto-deletion.lock-at-most-for=PT5S"
})
@Sql(scripts = {
        "/scripts/cleanup_database.sql",
        "/scripts/basic_corp_structure.sql",
        "/scripts/transport.sql",
        "/scripts/request_with_checks.sql",
        "/scripts/check_photo.sql"
})
class CheckPhotoSchedulerTest {
    
    @MockitoSpyBean
    private CheckPhotoService checkPhotoService;
    @MockitoBean
    private FileService fileService;
    
    @Test
    @SneakyThrows
    void deleteOldPhotos() {
        doNothing().when(fileService).delete(any());
        await().atMost(Duration.ofSeconds(10))
               .pollDelay(Duration.ofSeconds(2))
               .untilAsserted(() -> verify(checkPhotoService).deleteOutdatedPhotos());
    }
}
