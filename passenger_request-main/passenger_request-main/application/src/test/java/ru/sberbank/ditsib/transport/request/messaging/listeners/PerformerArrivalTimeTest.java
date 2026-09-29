package ru.sberbank.ditsib.transport.request.messaging.listeners;

import io.qameta.allure.Feature;
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
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.ExpectedData;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.TaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Тест получения времени подачи транспортного средства по API")
@Transactional
class PerformerArrivalTimeTest extends KafkaTest {

    @Autowired
    private RequestForTaxiRepository repository;

    @Autowired
    TaxiTripRepository taxiTripRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    TripPurposeRepository tripPurposeRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    @Qualifier("inContractorTaxiTripInProgressInput")
    Consumer<Message<InContractorTaxiTripInProgressMessage>> inContractorTaxiTripInProgressInput;

    @Test
    @DisplayName("Получение сообщения по API с указанной датой подачи ТС, водитель опоздал на 1 час")
    void handleTest_inProgress() {

        var organizationId = UUID.randomUUID();
        organizationRepository.save(Organization.builder().id(organizationId).active(true).officialName("officialName").digitId(1L).build());

        // Моделируем опоздание водителя на 1 час
        LocalDateTime performerArrivalTime = LocalDateTime.of(2023, 11, 1, 10, 0, 0);
        LocalDateTime driverArrivedDeadline = performerArrivalTime.minusHours(1);
        LocalDateTime desiredDateTime = driverArrivedDeadline.minusMinutes(15);

        String taxiId = "taxiId";
        Employee author = employeeRepository.save(Employee.builder()
                .id(UUID.randomUUID())
                .firstName("firstName")
                .lastName("lastName")
                .itinerantType(ItinerantType.NONE)
                .build());

        TripPurpose purpose = tripPurposeRepository.save(TripPurpose.builder().id(UUID.randomUUID()).purpose("test").build());

        var request = repository.save(RequestForTaxi
                .builder()
                .author(author)
                .organizationId(organizationId)
                .taxiClass(TaxiClass.ECONOMY)
                .humanReadableId("OT-0001-00000001")
                .driverArrivedDeadline(driverArrivedDeadline)
                .deadlineState(DeadlineState.NONE)
                .status(TripRequestStatus.TAXI_DRIVER_FOUND)
                .transportType(TransportTypeEnum.TAXI)
                .passenger(author)
                .desiredDate(desiredDateTime)
                .purpose(purpose)
                .tariff(TaxiTariff.builder().id(UUID.randomUUID()).build())
                .outcomeTariff(TaxiTariff.builder().id(UUID.randomUUID()).build())
                .creationTime(LocalDateTime.now())
                .expected(new ExpectedData(0.0d, 0.0d, 0L, 0.0d, Duration.ZERO))
                .contractorId(UUID.randomUUID())
                .build());

        var trip = taxiTripRepository.save(TaxiTrip
                .builder()
                .organizationId(organizationId)
                .taxiId(taxiId)
                .requests(new ArrayList<>(List.of(request)))
                .humanReadableId("TT-OT-0001-00000001")
                .status(InboundTaxiTripStatus.DRIVER_ON_THE_WAY)
                .tariffId(UUID.randomUUID())
                .build());
        final var message = Instancio.of(InContractorTaxiTripInProgressMessage.class)
                .set(field(InContractorTaxiTripInProgressMessage::humanId), trip.getHumanReadableId())
                .set(field(InContractorTaxiTripInProgressMessage::tripId), trip.getId().toString())
                .set(field(InContractorTaxiTripInProgressMessage::taxiId), trip.getTaxiId())
                .set(field(InContractorTaxiTripInProgressMessage::performerArrivalTime), performerArrivalTime)
                .set(field(InContractorTaxiTripInProgressMessage::resolution), "resolution")
                .set(field(InContractorTaxiTripInProgressMessage::transportType), TransportTypeEnum.TAXI)
                .set(field(InContractorTaxiTripInProgressMessage::status), InboundTaxiTripStatus.DRIVER_ON_THE_WAY)
                .set(field(InContractorTaxiTripInProgressMessage::driver), null)
                .create();
        inContractorTaxiTripInProgressInput.accept(MessageBuilder.withPayload(message).build());

        request = repository.getReferenceById(request.getId());

        assertEquals(performerArrivalTime, request.getDriverArrivedDatetime());
        assertEquals(DeadlineState.RED, request.getDeadlineState());
    }
}
