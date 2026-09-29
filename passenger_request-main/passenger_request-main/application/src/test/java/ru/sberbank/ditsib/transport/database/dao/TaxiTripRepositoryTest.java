package ru.sberbank.ditsib.transport.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.TaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.model.TaxiTrip;
import ru.sberbank.ditsib.transport.request.messaging.message.RequestFactDataMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;

@Transactional
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
class TaxiTripRepositoryTest extends KafkaTest {

    @Autowired
    private TaxiTripRepository taxiTripRepository;

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/request_for_taxi.sql",
    })
    void updateFactDataByHrId() {
        var message = of(RequestFactDataMessage.class)
                .set(field(RequestFactDataMessage::getHrId), "OT-0001-00023473")
                .create();

        taxiTripRepository.updateFactDataByHrId(
                message.getFactTotalWaitingTime(),
                message.getRegistryHrId(),
                message.getFactCost(),
                message.getFactDistance(),
                message.getIsPaid(),
                message.getHrId()
        );

        var updatedTaxiTrip = taxiTripRepository.findById(UUID.fromString("2a04c613-5b18-4267-9700-cfad231781a8"));
        assertThat(updatedTaxiTrip)
                .isPresent();
        assertThat(updatedTaxiTrip.get())
                .extracting(
                        TaxiTrip::getRegistryFactWaitingTime,
                        TaxiTrip::getRegistryHumanReadableId,
                        TaxiTrip::getRegistryFactCost,
                        TaxiTrip::getRegistryFactDistance,
                        TaxiTrip::getRegistryFactPayment
                )
                .containsExactly(
                        message.getFactTotalWaitingTime(),
                        message.getRegistryHrId(),
                        message.getFactCost(),
                        message.getFactDistance(),
                        message.getIsPaid()
                );
    }
}