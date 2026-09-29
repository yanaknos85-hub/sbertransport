package ru.sber.transport.telemechanic.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.DispatcherRepository;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = { "scheduler.dispatcher.cron=*/5 * * * * *",
                               "scheduler.dispatcher.lock-at-least-for=PT5S",
                               "scheduler.dispatcher.lock-at-most-for=PT5S" })
@EmbeddedPostgres
class DispatcherSchedulerTest {
    
    @Autowired
    private DispatcherRepository dispatcherRepository;
    
    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_tariff.sql",
            "/scripts/expired_dispatcher.sql"
    })
    void schedule() {
        await().atMost(Duration.ofSeconds(10)).pollDelay(Duration.ofSeconds(2))
               .until(() -> dispatcherRepository.findById(UUID.fromString("a8dd0938-d8ab-4ed7-8d82-1f5b46240fed"))
                                                .filter(value -> !value.isActive())
                                                .isPresent());
    }
}

