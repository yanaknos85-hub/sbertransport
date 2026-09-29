package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.LoggingExtension;
import ru.sberbank.ditsib.transport.request.database.dao.CoopTaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.dao.SingleTaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.model.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.request.mappers.TaxiTripMapper;
import ru.sberbank.ditsib.transport.request.messaging.message.TaxiTripMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.TaxiTripSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.doReturn;

@EmbeddedPostgres
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка отправки поездок на такси в Kafka")
class TaxiTripSenderImplTest extends KafkaTest {

    @Autowired
    private TaxiTripSender taxiTripSender;
    @MockitoBean
    private TaxiTripMapper taxiTripMapper;
    @MockitoBean
    private CoopTaxiTripRepository coopTaxiTripRepository;
    @MockitoBean
    private SingleTaxiTripRepository singleTaxiTripRepository;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(TaxiTripSenderImpl.class);

    @Test
    void send() {
        var message = Instancio.create(TaxiTripMessage.class);
        taxiTripSender.send(message);
        var actual = consumeMessage("service.request.taxi.trip", TaxiTripMessage.class);
        assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(message);
        checkLogs(message);
    }

    @Test
    void sendCoopTaxiTrip() {
        var taxiTrip = Instancio.create(CoopTaxiTrip.class);
        var message = Instancio.create(TaxiTripMessage.class);
        doReturn(taxiTrip).when(coopTaxiTripRepository).getReferenceById(taxiTrip.getId());
        doReturn(message).when(taxiTripMapper).toMessage(taxiTrip, false);
        taxiTripSender.send(taxiTrip);
        var actual = consumeMessage("service.request.taxi.trip", TaxiTripMessage.class);
        assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(message);
        checkLogs(message);
    }

    @Test
    void sendSingleTaxiTrip() {
        var taxiTrip = Instancio.create(SingleTaxiTrip.class);
        var message = Instancio.create(TaxiTripMessage.class);
        doReturn(taxiTrip).when(singleTaxiTripRepository).getReferenceById(taxiTrip.getId());
        doReturn(message).when(taxiTripMapper).toMessage(taxiTrip, false);
        taxiTripSender.send(taxiTrip);
        var actual = consumeMessage("service.request.taxi.trip", TaxiTripMessage.class);
        assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(message);
        checkLogs(message);
    }

    private static void checkLogs(TaxiTripMessage message) {
        assertThat(LOGGING_EXTENSION.getEvents())
                .hasSize(2)
                .extracting(
                        ILoggingEvent::getLevel,
                        ILoggingEvent::getMessage
                )
                .containsExactly(
                        tuple(
                                Level.INFO,
                                "Send message, taxiId:{}, tripId:{}, message:{}".formatted(message.getTaxiId(), message.getId(), message)
                        ),
                        tuple(
                                Level.INFO,
                                "Sent message, taxiId:{}, tripId:{}".formatted(message.getTaxiId(), message.getId())
                        )
                );
    }
}