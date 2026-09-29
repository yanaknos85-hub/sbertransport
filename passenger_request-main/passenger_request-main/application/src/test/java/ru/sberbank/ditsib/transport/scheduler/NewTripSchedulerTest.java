package ru.sberbank.ditsib.transport.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.service.ContractorTripService;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@EmbeddedPostgres
@SpringBootTest(
        classes = RequestApplication.class,
        properties = {
                "scheduler.new-trip.cron=*/1 * * * * *",
                "SCHEDULED_DEADLINE_CHECK=0 0 0 * * *"
        })
class NewTripSchedulerTest extends KafkaTest {

    @MockitoBean
    private ContractorTripService contractorTripService;

    @Test
    void schedule() {
        doNothing().when(contractorTripService).processNewTrips();
        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .pollDelay(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    verify(contractorTripService, atLeastOnce()).processNewTrips();
                });
    }
}
