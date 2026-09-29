package ru.sber.transport.telemechanic.integration;

import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.DriverRepository;
import ru.sber.transport.telemechanic.messaging.listener.message.DriverMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@SpringBootTest(properties = "extlogging.kafka: false")
@EmbeddedPostgres
class DriverListenerTest extends KafkaTest {
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql", "/scripts/driver_integration_test.sql" })
    @SneakyThrows
    @Transactional
    void listen_ShouldSaveDriver() {
        var driverId = UUID.randomUUID();
        var employeeId = UUID.fromString("3cd45c19-fd39-413c-99a9-30f35bd442a8"); // no driver by employee id
        
        var message = Instancio.of(DriverMessage.class)
                               .set(field(DriverMessage::id), driverId)
                               .set(field(DriverMessage::oauthId), employeeId)
                               .set(field(DriverMessage::autoparkId), UUID.randomUUID())
                               .set(field(DriverMessage::contractorId), UUID.randomUUID())
                               .set(field(DriverMessage::active), true)
                               .set(field(DriverMessage::tin), "123")
                               .set(field(DriverMessage::snils), "1231231312")
                               .set(field(DriverMessage::driverLicenseNumber), "40 51 123456")
                               .create();
        
        produceMessage("service.dispatcher.driver", message);
        var actual = driverRepository.findWithLicenseByEmployeeId(employeeId).orElseThrow();
        assertThat(actual.getAutoparkId()).isEqualTo(message.autoparkId());
        assertThat(actual.getContractorId()).isEqualTo(message.contractorId());
        assertThat(actual.getSnils()).isEqualTo(message.snils());
        assertThat(actual.getTin()).isEqualTo(message.tin());
        assertThat(actual.getDrivingLicense().getNumber()).isEqualTo("123456");
        assertThat(actual.getDrivingLicense().getSeries()).isEqualTo("40 51");
    }
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql", "/scripts/driver_integration_test.sql" })
    @SneakyThrows
    @Transactional
    void listen_ShouldSaveDriverWithOtherLicenseFormat() {
        var driverId = UUID.randomUUID();
        var employeeId = UUID.fromString("3cd45c19-fd39-413c-99a9-30f35bd442a8"); // no driver by employee id
        
        var message = Instancio.of(DriverMessage.class)
                               .set(field(DriverMessage::id), driverId)
                               .set(field(DriverMessage::oauthId), employeeId)
                               .set(field(DriverMessage::autoparkId), UUID.randomUUID())
                               .set(field(DriverMessage::contractorId), UUID.randomUUID())
                               .set(field(DriverMessage::active), true)
                               .set(field(DriverMessage::tin), "123")
                               .set(field(DriverMessage::snils), "1231231312")
                               .set(field(DriverMessage::driverLicenseNumber), "123456 40 51")
                               .create();
        
        produceMessage("service.dispatcher.driver", message);
        var actual = driverRepository.findWithLicenseByEmployeeId(employeeId).orElseThrow();
        assertThat(actual.getAutoparkId()).isEqualTo(message.autoparkId());
        assertThat(actual.getContractorId()).isEqualTo(message.contractorId());
        assertThat(actual.getSnils()).isEqualTo(message.snils());
        assertThat(actual.getTin()).isEqualTo(message.tin());
        assertThat(actual.getDrivingLicense().getNumber()).isEqualTo("123456");
        assertThat(actual.getDrivingLicense().getSeries()).isEqualTo("40 51");
    }
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql", "/scripts/driver_integration_test.sql" })
    @SneakyThrows
    @Transactional
    void listen_ShouldUpdateDriver() {
        var driverId = UUID.randomUUID();
        var employeeId = UUID.fromString("3cd45c19-fd39-413c-99a0-30f35bd442a8"); // driver exists by employee id
        
        var message = Instancio.of(DriverMessage.class)
                               .set(field(DriverMessage::id), driverId)
                               .set(field(DriverMessage::oauthId), employeeId)
                               .set(field(DriverMessage::autoparkId), UUID.randomUUID())
                               .set(field(DriverMessage::contractorId), UUID.randomUUID())
                               .set(field(DriverMessage::active), true)
                               .set(field(DriverMessage::tin), "123")
                               .set(field(DriverMessage::snils), "1231231312")
                               .set(field(DriverMessage::driverLicenseNumber), "123456 40 51")
                               .create();
        
        produceMessage("service.dispatcher.driver", message);
        var actual = driverRepository.findWithLicenseByEmployeeId(employeeId).orElseThrow();
        assertThat(actual.getAutoparkId()).isEqualTo(message.autoparkId());
        assertThat(actual.getContractorId()).isEqualTo(message.contractorId());
        assertThat(actual.getSnils()).isEqualTo(message.snils());
        assertThat(actual.getTin()).isEqualTo(message.tin());
        assertThat(actual.getDrivingLicense().getNumber()).isEqualTo("123456");
        assertThat(actual.getDrivingLicense().getSeries()).isEqualTo("40 51");
    }
}
