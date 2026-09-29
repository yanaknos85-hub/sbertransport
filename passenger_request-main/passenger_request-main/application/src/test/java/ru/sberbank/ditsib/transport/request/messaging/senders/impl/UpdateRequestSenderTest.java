package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.WaypointDTO;
import ru.sberbank.ditsib.transport.request.messaging.message.UpdateTripRequestMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.UpdateRequestSender;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка отправки изменений заявок в Kafka")
class UpdateRequestSenderTest extends KafkaTest {
    
    @Autowired
    private UpdateRequestSender sender;
    
    @MockitoBean("updateRequestOutput")
    private OutputBridge updateRequestOutput;
    
    @Test
    @DisplayName("Отправка")
    void test() {
        var address = new Address();
        address.setBuilding("Building");
        address.setCity("City");
        address.setCountry("Country");
        address.setHouse("House");
        address.setLatitude(0.5);
        address.setLongitude(1.5);
        address.setRegion("Region");
        address.setStreet("Street");
        address.setStructure("Structure");
        
        var waypoint = new Waypoint();
        waypoint.setAddress(address);
        
        var expected = new ExpectedData();
        expected.setCost(0.5);
        expected.setDistance(1.5);
        expected.setTime(Duration.ofDays(1));
        
        var purpose = new TripPurpose();
        purpose.setPurpose("Purpose");
        
        var request = new RequestForTaxi();
        request.setStatus(TripRequestStatus.TAXI_CANCELLED);
        request.setAuthor(Employee.builder().id(UUID.randomUUID()).build());
        request.setPassenger(Employee.builder().id(UUID.randomUUID()).build());
        request.setCommentForDriver("comment");
        request.setCoopTrip(true);
        request.setCreationTime(LocalDateTime.now().plusDays(1));
        request.setDesiredDate(LocalDateTime.now().plusDays(2));
        request.setExpected(expected);
        request.setFinishedTime(LocalDateTime.now().plusDays(3));
        request.setHumanReadableId("HRI");
        request.setId(UUID.randomUUID());
        request.setPassengerCount(4);
        request.setPurpose(purpose);
        request.setTariffId(UUID.randomUUID());
        request.setTaxiClass(TaxiClass.COMFORT);
        request.setTransportType(TransportTypeEnum.TAXI);
        request.getWaypoints().add(waypoint);
        
        var updateExpected = new ExpectedData();
        updateExpected.setCost(0.5);
        updateExpected.setDistance(1.5);
        updateExpected.setTime(Duration.ofDays(1));
        
        var waypointDTO = WaypointDTO.builder()
                                     .building("Building").
                                     city("City")
                                     .country("Country")
                                     .house("House")
                                     .latitude(0.5)
                                     .longitude(1.5)
                                     .region("Region")
                                     .street("Street")
                                     .structure("Structure")
                                     .checkinManual(true)
                                     .absenceReason("Reason")
                                     .checkinAutomatic(true)
                                     .build();
        
        var updateRequest = UpdateRequest.builder()
                                         .id(UUID.randomUUID())
                                         .expected(updateExpected)
                                         .waypoints(Collections.singletonList(waypointDTO))
                                         .request(request).build();
        
        sender.send(updateRequest);
        sender.sendDeleted(updateRequest);
        
        var messageCaptor = ArgumentCaptor.forClass(UpdateTripRequestMessage.class);
        verify(updateRequestOutput, times(2)).send(messageCaptor.capture());
        var actual = messageCaptor.getAllValues().getFirst();
        
        assertThat(actual.getId()).isEqualTo(updateRequest.getId());
        assertThat(actual.getRequest().getStatus()).isEqualTo(request.getStatus().name());
        assertThat(actual.getRequest().getAuthorId()).isEqualTo(request.getAuthor().getId());
        assertThat(actual.getRequest().getCreationTime()).isEqualTo(request.getCreationTime());
        assertThat(actual.getRequest().getDesiredDate()).isEqualTo(request.getDesiredDate());
        assertThat(actual.getRequest().getHumanReadableId()).isEqualTo(request.getHumanReadableId());
        assertThat(actual.getRequest().getId()).isEqualTo(request.getId());
        assertThat(actual.getRequest().getPurposeId()).isEqualTo(request.getPurpose().getId());
        assertThat(actual.getRequest().getTariffId()).isEqualTo(request.getTariffId());
        assertThat(actual.getRequest().getTransportType()).isEqualTo(request.getTransportType().name());
        assertThat(actual.getRequest().isDeleted()).isFalse();
        assertThat(waypointDTO.getAbsenceReason()).isEqualTo("Reason");
        assertThat(waypointDTO.isCheckinAutomatic()).isTrue();
        assertThat(waypointDTO.isCheckinManual()).isTrue();
        
        assertThat(actual.getRequest().getExpected().getCost()).isEqualTo(request.getExpected().getCost());
        assertThat(actual.getRequest().getExpected().getDistance()).isEqualTo(request.getExpected().getDistance());
        assertThat(actual.getRequest().getExpected().getTime()).isEqualTo(request.getExpected().getTime());
        
        assertThat(actual.getRequest().getWaypoints().getFirst().getWaitTime())
                .isEqualTo(request.getWaypoints().getFirst().getWaitTime());
        assertThat(actual.getRequest().getWaypoints().getFirst().getAddress().getBuilding()).isEqualTo(address.getBuilding());
        assertThat(actual.getRequest().getWaypoints().getFirst().getAddress().getCity()).isEqualTo(address.getCity());
        assertThat(actual.getRequest().getWaypoints().getFirst().getAddress().getCountry()).isEqualTo(address.getCountry());
        assertThat(actual.getRequest().getWaypoints().getFirst().getAddress().getHouse()).isEqualTo(address.getHouse());
        assertThat(actual.getRequest().getWaypoints().getFirst().getAddress().getLatitude()).isEqualTo(address.getLatitude());
        assertThat(actual.getRequest().getWaypoints().getFirst().getAddress().getLongitude()).isEqualTo(address.getLongitude());
        assertThat(actual.getRequest().getWaypoints().getFirst().getAddress().getRegion()).isEqualTo(address.getRegion());
        assertThat(actual.getRequest().getWaypoints().getFirst().getAddress().getStreet()).isEqualTo(address.getStreet());
        assertThat(actual.getRequest().getWaypoints().getFirst().getAddress().getStructure()).isEqualTo(address.getStructure());
        assertThat(actual.isDeleted()).isFalse();
        
        actual = messageCaptor.getAllValues().get(1);
        assertThat(actual.isDeleted()).isTrue();
        
    }
    
}
