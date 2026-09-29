package ru.sberbank.transport.oto.cargo.messaging;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.enums.RequestTypeEnum;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.cargo.CargoTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.AddressMessage;
import ru.sberbank.ditsib.transport.request.messaging.CargoRequestMessage;
import ru.sberbank.ditsib.transport.request.messaging.CargoRequestMultiMessage;
import ru.sberbank.transport.oto.cargo.database.dao.*;
import ru.sberbank.transport.oto.cargo.database.model.Employee;
import ru.sberbank.transport.oto.cargo.database.model.Waypoint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка получения данных заявки")
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class TripRequestListenerTest extends KafkaTest {
    public static final String HUMAN_READABLE_ID = "HRI";

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private WaypointContactRepository waypointContactRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private WaypointRepository waypointRepository;

    @AfterEach
    void dropRepository() {
        waypointContactRepository.deleteAll();
        requestRepository.deleteAll();
        waypointRepository.deleteAll();
        employeeRepository.deleteAll();
        contractorRepository.deleteAll();
        addressRepository.deleteAll();
    }

    @Test
    @DisplayName("Новая заявка по грузоперевозкам")
    void addCargoRequestTest() {
        var msg = CargoRequestMessage.builder()
                                     .id(UUID.randomUUID())
                                     .approvalId(UUID.randomUUID())
                                     .deleted(false)
                                     .authorId(UUID.randomUUID())
                                     .creationTime(LocalDateTime.now())
                                     .desiredDate(LocalDateTime.now().plusDays(1))
                                     .transferTime(LocalDateTime.now().plusDays(2))
                                     .shipmentTime(LocalDateTime.now().plusDays(3))
                                     .humanReadableId(HUMAN_READABLE_ID)
                                     .senderId(UUID.randomUUID())
                                     .recipientId(UUID.randomUUID())
                                     .status(TripRequestStatus.CARGO_APPROVED.name())
                                     .transportType(TransportTypeEnum.COURIER.name())
                                     .waypoints(Collections.emptyList())
                                     .cargoTripId(UUID.randomUUID())
                                     .source("UNDEFINED")
                                     .volume(7D)
                                     .weight(7D)
                                     .waypoints(List.of(CargoRequestMessage.Waypoint.builder()
                                                                                    .id(UUID.randomUUID())
                                                                                    .address(AddressMessage.builder().id(UUID.randomUUID())
                                                                                                           .addressStringRepresentation("test")
                                                                                                           .build())
                                                                                    .checkinAutomatic(true)
                                                                                    .checkinManual(false)
                                                                                    .orderingIndex(0).build(),
                                                        CargoRequestMessage.Waypoint.builder().id(UUID.randomUUID())
                                                                                    .address(AddressMessage.builder().id(UUID.randomUUID())
                                                                                                           .addressStringRepresentation("test")
                                                                                                           .build())
                                                                                    .checkinAutomatic(true)
                                                                                    .checkinManual(false)
                                                                                    .orderingIndex(1).build()))
                                     .build();

        Map<String, Object> headers = Map.of(KafkaHeaders.RECEIVED_KEY, msg.getId(),
                                             "transportType", msg.getTransportType());
        produceMessage("service.request.cargo", MessageBuilder.createMessage(msg, new MessageHeaders(headers)));
        checkRequest(TransportTypeEnum.COURIER.name(), TripRequestStatus.CARGO_APPROVED.name(), 0, RequestTypeEnum.SINGLE);
    }

    @Test
    @DisplayName("Новая заявка по грузоперевозкам")
    void addCargoRequest2Test() {
        var id = UUID.randomUUID();
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var id3 = UUID.randomUUID();
        var id4 = UUID.randomUUID();
        var id5 = UUID.randomUUID();
        var id6 = UUID.randomUUID();
        var id7 = UUID.randomUUID();
        var id8 = UUID.randomUUID();

        Employee e = Employee.builder().id(id4).firstName("Иван").lastName("Иванов").mobilePhone("8(800)123-4567").build();


        var address1 = AddressMessage.builder()
                                     .id(UUID.nameUUIDFromBytes("Санкт-Петербург".getBytes()))
                                     .country("Россия")
                                     .region("Санкт-Петербург")
                                     .city("Санкт-Петербург")
                                     .street("Улица")
                                     .house("1")
                                     .building(null)
                                     .addressStringRepresentation("Россия, Санкт-Петербург, Улица, 1")
                                     .structure(null)
                                     .existInVspGosbTbRegistry(true)
                                     .build();

        var address2 = AddressMessage.builder()
                                     .id(UUID.nameUUIDFromBytes("Санкт-Петербург".getBytes()))
                                     .country("Россия")
                                     .region("Санкт-Петербург")
                                     .city("Санкт-Петербург")
                                     .street("Улица")
                                     .house("2")
                                     .building(null)
                                     .addressStringRepresentation("Россия, Санкт-Петербург, Улица, 2")
                                     .structure(null)
                                     .existInVspGosbTbRegistry(true)
                                     .build();

        var address3 = AddressMessage.builder()
                                     .id(UUID.nameUUIDFromBytes("Санкт-Петербург".getBytes()))
                                     .country("Россия")
                                     .region("Санкт-Петербург")
                                     .city("Санкт-Петербург")
                                     .street("Улица")
                                     .house("3")
                                     .building(null)
                                     .addressStringRepresentation("Россия, Санкт-Петербург, Улица, 3")
                                     .structure(null)
                                     .existInVspGosbTbRegistry(true)
                                     .build();

        var contact = CargoRequestMultiMessage.Contact.builder()
                                                      .id(id7)
                                                      .mobilePhone("8(800)123-4567")
                                                      .fullname("Иван Иванов")
                                                      .employeeId(id4)
                                                      .build();

        var contact1 = CargoRequestMultiMessage.Contact.builder()
                                                       .id(id1)
                                                       .mobilePhone("8(800)123-4567")
                                                       .fullname("Иван Иванов")
                                                       .employeeId(id4)
                                                       .build();

        var contact2 = CargoRequestMultiMessage.Contact.builder()
                                                       .id(id2)
                                                       .mobilePhone("8(800)123-4567")
                                                       .fullname("Иван Иванов")
                                                       .employeeId(id4)
                                                       .build();


        var waypoint1 = CargoRequestMultiMessage.Waypoint.builder()
                                                         .id(id7)
                                                         .address(address1)
                                                         .waitTime(Duration.ofHours(1))
                                                         .checkinAutomatic(false)
                                                         .checkinManual(false)
                                                         .absenceReason("Rain")
                                                         .orderingIndex(0)
                                                         .typePoint("LOAD")
                                                         .contacts(List.of(contact))
                                                         .build();

        var waypoint2 = CargoRequestMultiMessage.Waypoint.builder()
                                                         .id(id1)
                                                         .address(address2)
                                                         .waitTime(Duration.ofHours(1))
                                                         .checkinAutomatic(false)
                                                         .checkinManual(false)
                                                         .absenceReason("Rain")
                                                         .orderingIndex(1)
                                                         .typePoint("LOAD")
                                                         .contacts(List.of(contact1))
                                                         .build();

        var waypoint3 = CargoRequestMultiMessage.Waypoint.builder()
                                                         .id(id2)
                                                         .address(address3)
                                                         .waitTime(Duration.ofHours(1))
                                                         .checkinAutomatic(false)
                                                         .checkinManual(false)
                                                         .absenceReason("Rain")
                                                         .orderingIndex(2)
                                                         .typePoint("LOAD")
                                                         .contacts(List.of(contact2))
                                                         .build();

        var msg = CargoRequestMultiMessage.builder()
                                          .id(id)
                                          .cargoTypeId(id6)
                                          .requestType(RequestTypeEnum.RELOCATION)
                                          .approvalDate(LocalDateTime.now())
                                          .approvalId(id5)
                                          .commentForDriver("commentForDriver")
                                          .senderOrganization("орг1")
                                          .recipientOrganization("орг2")
                                          .length(40.0)
                                          .volume(28000.0)
                                          .weight(15.0)
                                          .height(23.0)
                                          .width(67.0)
                                          .occupiedPlacesCount(5)
                                          .cargoDetails(Collections.singletonList(CargoRequestMultiMessage.CargoDetail.builder()
                                                                                                                      .id(id3)
                                                                                                                      .cargoName("nameCargo")
                                                                                                                      .cargoCategory("CAT")
                                                                                                                      .cargoType(
                                                                                                                              CargoTypeEnum.DOCUMENT.name())
                                                                                                                      .build()))

                                          .author(CargoRequestMultiMessage.Employee.builder().id(id4).fullName("Full Name")
                                                                                   .mobilePhone("7123123123123").organizationId(UUID.randomUUID())
                                                                                   .build())
                                          .contractorId(id8)
                                          .waypoints(List.of(waypoint1, waypoint2, waypoint3))
                                          .statusCode(1)
                                          .desiredDate(LocalDateTime.now().plusDays(1))
                                          .creationTime(LocalDateTime.now())
                                          .approvalState("approvalState")
                                          .cost(24)
                                          .distance(100)
                                          .expected(CargoRequestMultiMessage.ExpectedData.builder()
                                                                                         .time(Duration.ofDays(1))
                                                                                         .deliveryTime(2)
                                                                                         .build())
                                          .finishedTime(LocalDateTime.now().plusDays(5))
                                          .status(TripRequestStatus.CARGO_APPROVED.name())
                                          .sender(CargoRequestMultiMessage.Employee.builder().fullName("Ivan Ivan").mobilePhone("+03").build())
                                          .recipient(CargoRequestMultiMessage.Employee.builder().fullName("Petr Petrov").mobilePhone("+02").build())
                                          .savingsCash(123L)
                                          .humanReadableId(HUMAN_READABLE_ID)
                                          .tariffId(UUID.randomUUID())
                                          .transportType(TransportTypeEnum.PUBLIC.name())
                                          .loaders(5)
                                          .build();

        employeeRepository.saveAndFlush(e);
        Map<String, Object> headers = Map.of(KafkaHeaders.RECEIVED_KEY, msg.getId(),
                                             "transportType", msg.getTransportType());
        produceMessage("service.request.cargo2", msg, headers);
        checkRequest(TransportTypeEnum.PUBLIC.name(), TripRequestStatus.CARGO_APPROVED.name(), 5, RequestTypeEnum.RELOCATION);
    }

    private void checkRequest(String transportType, String status, Integer loaders, RequestTypeEnum requestType) {
        assertThat(requestRepository.count()).isEqualTo(1);
        var actual = requestRepository.findAll().getFirst();
        assertThat(actual.getHumanReadableId()).isEqualTo(HUMAN_READABLE_ID);
        assertThat(actual.getTransportType()).isEqualTo(transportType);
        assertThat(actual.getStatus()).isEqualTo(status);
        assertThat(actual.getLoaders()).isEqualTo(loaders);
        assertThat(actual.getRequestType()).isEqualTo(
                Optional.ofNullable(requestType).map(RequestTypeEnum::name).orElse(null));

        List<Waypoint> waypoints = actual.getWaypoints();

        waypoints.stream().min(Comparator.comparingInt(Waypoint::getOrderingIndex)).ifPresent(w -> assertThat(w.getId())
                .isEqualTo(actual.getStartWaypoint().getId()));

        waypoints.stream().max(Comparator.comparingInt(Waypoint::getOrderingIndex)).ifPresent(w -> assertThat(w.getId())
                .isEqualTo(actual.getEndWaypoint().getId()));
    }
}
