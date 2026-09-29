package ru.sberbank.ditsib.transport.vehicle.messaging.listeners;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.vehicle.database.dao.OdometerHistoryRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.OdometerValueRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.TransportRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.OdometerHistory;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.OdometerHistoryValueMessage;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres
public class OdometerHistoryListenerTest extends KafkaTest {
    
    @Autowired
    private TransportRepository transportRepository;
    @Autowired
    private OdometerHistoryRepository odometerHistoryRepository;
    @Autowired
    private OdometerValueRepository odometerValueRepository;
    
    @Test
    @SneakyThrows
    @Sql("/scripts/vehicle_integration_test.sql")
    @Sql("/scripts/transport_integration_test.sql")
    @Sql("/scripts/transport_report_integration_test.sql")
    void listen() {
        var transportId = UUID.fromString("bc3f3b07-f136-4270-af0b-ff84af381ac4");
        var transport = transportRepository.findById(transportId)
                                           .orElseThrow(() -> new JUnitException("Transport not found"));
        assertThat(transport.getCurrentMileage()).isEqualTo(6);
        var message = new OdometerHistoryValueMessage(
                transportId,
                1000,
                UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                LocalDateTime.of(2000, 1, 1, 1, 1, 1),
                null
        );
        produceMessage("service.odometer.history", message);
        
        var odometerHistory = odometerHistoryRepository.findAll().stream()
                .filter(entity -> entity.getTransportId().equals(transportId))
                .toList();
        assertThat(odometerHistory).hasSize(1);
        assertThat(odometerHistory.get(0))
                .extracting(OdometerHistory::getValue, OdometerHistory::getTransportId, OdometerHistory::getCreatorUserId, OdometerHistory::getCreationTime)
                .containsExactly(1000, transportId, UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"), LocalDateTime.of(2000, 1, 1, 1, 1, 1));
        
        var odometerValue = odometerValueRepository.findByTransportId(transportId);
        assertThat(odometerValue).hasSize(1);
        assertThat(odometerValue.get(0).getValue()).isEqualTo(1000);
        
        transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new JUnitException("Transport not found"));
        assertThat(transport.getCurrentMileage()).isEqualTo(1000);
    }
}
