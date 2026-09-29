package ru.sber.transport.telemechanic.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.EwbHistoryRepository;
import ru.sber.transport.telemechanic.database.dao.EwbRepository;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;

import java.time.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = { "scheduler.status.ewb.cron=*/5 * * * * *",
                               "scheduler.status.ewb.lock-at-least-for=PT5S",
                               "scheduler.status.ewb.lock-at-most-for=PT5S" })
@EmbeddedPostgres
class EwbStatusUpdateSchedulerTest {
    
    @Autowired
    EwbRepository ewbRepository;
    @Autowired
    EwbHistoryRepository ewbHistoryRepository;
    @MockitoSpyBean
    EwbHistoryRepository ewbHistoryRepositorySpy;
    @MockitoBean
    private Clock clock;
    
    @Test
    @DisplayName("Авто обновление статуса EWB")
    @Sql(scripts = { "/scripts/basic_corp_structure.sql",
                     "/scripts/ewb_integration_test.sql"
    })
    void updateStatus() {
        var fixedClock = Clock.fixed(LocalDateTime.of(2024, 8, 1, 23, 59)
                                                  .atZone(ZoneOffset.UTC)
                                                  .toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        await().atMost(Duration.ofSeconds(10)).pollDelay(Duration.ofSeconds(2))
               .untilAsserted(() -> verify(ewbHistoryRepositorySpy).saveAll(anyList()));
        
        var ewb = ewbRepository.findById(UUID.fromString("29d4ef98-51fa-43b3-b3ed-a9870ad4b659"));
        assertThat(ewb).isPresent();
        assertThat(ewb.get().getStatus()).isEqualTo(EwbStatus.EXPIRED);
        
        var history = ewbHistoryRepository.findAll();
        
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getStatus()).isEqualTo(EwbStatus.EXPIRED);
    }
}