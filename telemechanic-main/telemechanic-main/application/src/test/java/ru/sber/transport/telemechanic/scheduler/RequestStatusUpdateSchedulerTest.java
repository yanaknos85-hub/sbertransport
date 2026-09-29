package ru.sber.transport.telemechanic.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.RequestHistoryRepository;
import ru.sber.transport.telemechanic.database.dao.RequestRepository;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.time.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = { "scheduler.status.request.cron=*/1 * * * * *",
                               "scheduler.status.request.lock-at-least-for=PT5S",
                               "scheduler.status.request.lock-at-most-for=PT5S" })
@EmbeddedPostgres
class RequestStatusUpdateSchedulerTest {
    
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private RequestHistoryRepository requestHistoryRepository;
    @MockitoSpyBean
    private RequestHistoryRepository historyRepositorySpy;
    @MockitoBean
    private Clock clock;
    
    @Test
    @DisplayName("Авто обновление статуса технической заявки")
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void updateStatus() {
        var fixedClock = Clock.fixed(LocalDateTime.of(2023, 7, 20, 23, 59)
                                                  .atZone(ZoneOffset.UTC)
                                                  .toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(100))
                .pollDelay(Duration.ofSeconds(1))
                .untilAsserted(() -> verify(historyRepositorySpy, atLeastOnce()).saveAll(anyList()));
        
        var request = requestRepository.findById(UUID.fromString("d9d50a1e-1fc4-4458-ade6-f5297304a389"));
        assertThat(request).isPresent();
        assertThat(request.get().getStatus()).isEqualTo(RequestStatus.EXPIRED);
        
        var history = requestHistoryRepository.findAll();
        
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getStatus()).isEqualTo(RequestStatus.EXPIRED);
    }
}