package ru.sber.transport.telemechanic.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.DriverRepository;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = { "scheduler.driver.cron=*/5 * * * * *",
                               "scheduler.driver.lock-at-least-for=PT5S",
                               "scheduler.driver.lock-at-most-for=PT5S" })
@EmbeddedPostgres
class DriverSchedulerTest {
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/ewb_tariff.sql",
            "/scripts/driver.sql",
            "/scripts/expired_driver.sql"
    })
    void schedule() {
        await().atMost(Duration.ofSeconds(10)).pollDelay(Duration.ofSeconds(2))
               .until(() -> driverRepository.findById(UUID.fromString("5c2111d7-40d7-405c-bc73-2980c5b633ed"))
                                            .filter(value -> !value.getDrivingLicense().isActive())
                                            .isPresent());
    }
}
