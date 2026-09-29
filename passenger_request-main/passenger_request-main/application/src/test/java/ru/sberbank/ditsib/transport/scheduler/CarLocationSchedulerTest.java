package ru.sberbank.ditsib.transport.scheduler;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.messaging.message.CarLocationMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.CarLocationSender;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@EmbeddedPostgres
@SpringBootTest(
        classes = RequestApplication.class,
        properties = {
                "scheduler.car-location.cron=*/1 * * * * *",
                "scheduler.car-location.lock-at-least-for=PT3S",
                "scheduler.car-location.lock-at-most-for=PT10S",
                "SCHEDULED_DEADLINE_CHECK=0 0 0 * * *"
        })
class CarLocationSchedulerTest extends KafkaTest {

    @MockitoBean
    private CarLocationSender carLocationSender;
    @Captor
    private ArgumentCaptor<CarLocationMessage> carLocationMessageArgumentCaptor;

    private static final UUID CONTRACTOR_ID = UUID.fromString("bc5f6b63-a646-4090-9644-2f2abc79d4d4");
    private static final String ORDER_ID = "TP-0320-00000484";

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/request_for_taxi.sql",
            "/scripts/car_location_task.sql"
    })
    void schedule() {
        doNothing().when(carLocationSender).send(any(CarLocationMessage.class));
        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .pollDelay(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    verify(carLocationSender, atLeastOnce()).send(carLocationMessageArgumentCaptor.capture());
                    var sentIds = carLocationMessageArgumentCaptor.getAllValues();
                    boolean found = sentIds.stream()
                            .anyMatch(message ->
                                    message.contractorRequests() != null &&
                                    message.contractorRequests().containsKey(CONTRACTOR_ID) &&
                                    message.contractorRequests().get(CONTRACTOR_ID).orderPartnerIds().contains(ORDER_ID)
                            );
                    assertThat(found).isTrue();
                });
    }
}
