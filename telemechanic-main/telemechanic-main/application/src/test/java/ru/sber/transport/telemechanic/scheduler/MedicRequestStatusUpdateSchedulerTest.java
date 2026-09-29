package ru.sber.transport.telemechanic.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.MedicRequestHistoryRepository;
import ru.sber.transport.telemechanic.database.dao.MedicRequestRepository;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.time.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = { "scheduler.status.medic-request.cron=* * * * * *",
                               "scheduler.status.medic-request.lock-at-least-for=PT5S",
                               "scheduler.status.medic-request.lock-at-most-for=PT5S" })
@EmbeddedPostgres
class MedicRequestStatusUpdateSchedulerTest {
    
    @MockitoBean
    private Clock clock;
    @Autowired
    private MedicRequestRepository medicRequestRepository;
    @Autowired
    private MedicRequestHistoryRepository historyRepository;
    @MockitoSpyBean
    private MedicRequestHistoryRepository historyRepositorySpy;
    @Autowired
    private MedicRequestStatusUpdateScheduler medicRequestStatusUpdateScheduler;
    
    @Test
    @DisplayName("Авто обновление статуса мед заявки")
    @Sql(scripts = { "/scripts/cleanup_database.sql",
                     "/scripts/basic_corp_structure.sql",
                     "/scripts/ewb_integration_test.sql"
    })
    void updateStatus() {
        var fixedClock = Clock.fixed(LocalDateTime.of(2024, 10, 11, 23, 59)
                                                  .atZone(ZoneOffset.UTC)
                                                  .toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        await().atMost(Duration.ofSeconds(15)).pollDelay(Duration.ofSeconds(2))
               .untilAsserted(() -> verify(historyRepositorySpy).saveAll(anyList()));
        
        medicRequestStatusUpdateScheduler.updateStatus();
        
        var medicRequest = medicRequestRepository.findById(UUID.fromString("b5a6b2cf-06d3-41f9-9571-43e86ffffa71"));
        
        assertThat(medicRequest).isPresent();
        assertThat(medicRequest.get().getStatus()).isEqualTo(TelemedicineStatus.EXPIRED);
        
        var history = historyRepository.findAll();
        
        assertThat(history).hasSize(1);
        assertThat(history.getFirst().getStatus()).isEqualTo(TelemedicineStatus.EXPIRED);
    }
}