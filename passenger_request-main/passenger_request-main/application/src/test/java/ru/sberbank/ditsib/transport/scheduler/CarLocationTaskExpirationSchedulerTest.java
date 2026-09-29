package ru.sberbank.ditsib.transport.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.CarLocationTaskRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doReturn;

@EmbeddedPostgres
@SpringBootTest(
        classes = RequestApplication.class,
        properties = {
                "scheduler.car-location-task-expiration.cron=*/3 * * * * *",
                "scheduler.car-location-task-expiration.lock-at-least-for=PT3S",
                "scheduler.car-location-task-expiration.lock-at-most-for=PT5S",
                "SCHEDULED_DEADLINE_CHECK=0 0 0 * * *"
        }
)
class CarLocationTaskExpirationSchedulerTest extends KafkaTest {

    @MockitoBean
    private Clock clock;
    @Autowired
    private CarLocationTaskRepository carLocationTaskRepository;

    private static final LocalDateTime NOW = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/request_for_taxi.sql",
            "/scripts/car_location_task.sql"
    })
    void schedule() {
        var initTasksInDatabase = carLocationTaskRepository.findAll();
        assertThat(initTasksInDatabase).hasSize(2);

        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();

        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .pollDelay(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    var tasksInDatabase = carLocationTaskRepository.findAll();
                    assertThat(tasksInDatabase).isEmpty();
                });
    }
}
