package ru.sberbank.ditsib.transport.request.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.request.database.dao.TaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.model.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SpringBootTest(classes = RequestApplication.class)
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка обновления данных поездки по сообщению из Kafka")
class InProgressMessageProcessorTest extends KafkaTest {

    @Autowired
    private TaxiTripRepository taxiTripRepository;
    @Autowired
    @Qualifier("inContractorTaxiTripInProgressInput")
    private Consumer<Message<InContractorTaxiTripInProgressMessage>> input;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Test
    @DisplayName("Проверка обработки сообщения из Kafka")
    void test_handle() {
        final var organization = organizationRepository.save(Instancio.create(Organization.class));

        final var taxiTrip = SingleTaxiTrip.builder()
                .active(true)
                .status(InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)
                .organizationId(organization.getId())
                .humanReadableId(UUID.randomUUID().toString())
                .tariffId(UUID.randomUUID())
                .requests(Collections.emptyList())
                .build();

        final var saved = taxiTripRepository.save(taxiTrip);

        assertThat(taxiTripRepository.count()).isEqualTo(1L);
        assertThat(taxiTripRepository.findAll()).hasSize(1);

        final var message = Instancio.of(InContractorTaxiTripInProgressMessage.class)
                .set(field(InContractorTaxiTripInProgressMessage::humanId), saved.getHumanReadableId())
                .set(field(InContractorTaxiTripInProgressMessage::transportType), TransportTypeEnum.TAXI)
                .set(field(InContractorTaxiTripInProgressMessage::status), InboundTaxiTripStatus.DRIVER_ASSIGNED)
                .create();

        input.accept(MessageBuilder.withPayload(message).build());

        assertThat(taxiTripRepository.count()).isEqualTo(1L);

        final var actualList = taxiTripRepository.findAll();

        assertThat(actualList).hasSize(1);

        final var actual = actualList.getFirst();
        final var driverString = objectMapper.writeValueAsString(message.driver());

        assertSoftly(it -> {
            it.assertThat(actual.getTaxiId()).isEqualTo(message.taxiId());
            it.assertThat(actual.getDriver()).isEqualTo(driverString);
            it.assertThat(actual.getResolution()).isEqualTo(message.resolution());
            it.assertThat(actual.getHumanReadableId()).isEqualTo(message.humanId());
            it.assertThat(actual.getTripFactDistance()).isEqualTo(message.distance());
            it.assertThat(actual.getTripFactPrice()).isEqualTo(message.price().intValue());
            it.assertThat(actual.getAssignedCar().getBrandName()).isEqualTo(message.vehicle().mark());
            it.assertThat(actual.getAssignedCar().getColor()).isEqualTo(message.vehicle().color());
            it.assertThat(actual.getAssignedCar().getModel()).isEqualTo(message.vehicle().model());
            it.assertThat(actual.getAssignedCar().getRegistrationNumber()).isEqualTo(message.vehicle().registrationNumber());
        });
    }
}
