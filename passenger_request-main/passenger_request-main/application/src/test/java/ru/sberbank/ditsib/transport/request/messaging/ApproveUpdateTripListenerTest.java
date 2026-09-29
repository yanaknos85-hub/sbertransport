package ru.sberbank.ditsib.transport.request.messaging;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
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
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.messaging.message.ApproveUpdateTripRequestMessage;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@MockitoBean(types = JwtDecoder.class)
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Проверка получения аппрувов изменений маршрута")
@Transactional
@Slf4j
class ApproveUpdateTripListenerTest extends SharedTest {
    
    @Autowired
    @Qualifier("approveUpdateTripInput")
    private Consumer<Message<ApproveUpdateTripRequestMessage>> approveUpdateTripInput;
    
    @Autowired
    private RequestRepository requestRepository;
    
    @Autowired
    private UpdateRequestRepository updateRequestRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private TripPurposeRepository tripPurposeRepository;

    @Autowired
    private ContractorRepository contractorRepository;
    
    @BeforeEach
    void init() {
        departmentRepository.save(department1);
    }
    
    @Test
    @DisplayName("Approve")
    void handleApprove() {
        // prepare data
        employeeRepository.saveAndFlush(testEmployee1);
        employeeRepository.saveAndFlush(testEmployee2);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        request1.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        request1.setContractorId(contractor.getId());
        request1.setPurpose(createTripPurpose());
        request1.setHumanReadableId("123");
        request1.setStatus(TripRequestStatus.TAXI_APPROVED);
        requestRepository.save(request1);
        
        // prepare update data
        final ExpectedData expectedData =
                ExpectedData.builder().time(Duration.ofHours(1)).distance(1.0).cost(2.0).build();
        final UpdateRequest updateRequest = UpdateRequest.builder()
                                                         .request(request1)
                                                         .segmentsJSON(Arrays.asList(createSegmentDTO(50)))
                                                         .waypoints(Arrays.asList(
                                                                 createWaypointDTO("w1_test_alsd-1", 60),
                                                                 createWaypointDTO("w1_test_alsd-2", 70)))
                                                         .expected(expectedData)
                                                         .build();
        updateRequestRepository.save(updateRequest);
        

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(updateRequestRepository.count()).isEqualTo(1);
        List<Waypoint> waypoints = getOrderedWaypoints(requestRepository.findAll().getFirst());
        assertThat(waypoints.get(0).getAddress().getCountry()).doesNotEndWith("w1_test_alsd-1");
        assertThat(waypoints.get(1).getAddress().getCountry()).doesNotEndWith("w1_test_alsd-2");
        

        var message = ApproveUpdateTripRequestMessage.builder()
                                                     .approved(true)
                                                     .updateId(updateRequestRepository.findAll().getFirst().getId())
                                                     .approvedByEmployeeId(testEmployee1.getId())
                                                     .build();
        
        approveUpdateTripInput.accept(MessageBuilder.withPayload(message).build());
        

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(updateRequestRepository.count()).isZero();
        waypoints = getOrderedWaypoints(requestRepository.findAll().getFirst());
        assertThat(waypoints.get(0).getAddress().getCountry()).endsWith("w1_test_alsd-1");
        assertThat(waypoints.get(1).getAddress().getCountry()).endsWith("w1_test_alsd-2");
    }
    
    private static List<Waypoint> getOrderedWaypoints(Request request) {
        return request.getWaypoints().stream()
                      .sorted(Comparator.comparing(Waypoint::getOrderingIndex))
                      .collect(Collectors.toList());
    }
    
    @Test
    @DisplayName("Decline")
    void handleDecline() {
        employeeRepository.saveAndFlush(testEmployee1);
        employeeRepository.saveAndFlush(testEmployee2);
        request1.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        request1.setContractorId(contractor.getId());
        request1.setPurpose(createTripPurpose());
        request1.setHumanReadableId("123");
        request1.setStatus(TripRequestStatus.TAXI_APPROVED);
        request1.getExpected().setTime(Duration.ofHours(10));
        request1.getExpected().setDistance(20.0);
        request1.getExpected().setCost(30.0);
        requestRepository.save(request1);

        final ExpectedData expectedData =
                ExpectedData.builder().time(Duration.ofHours(1)).distance(1.0).cost(2.0).build();
        final UpdateRequest updateRequest = UpdateRequest.builder()
                                                         .request(request1)
                                                         .segmentsJSON(Arrays.asList(createSegmentDTO(50)))
                                                         .waypoints(Arrays.asList(createWaypointDTO("w1", 60),
                                                                                  createWaypointDTO("w2", 70)))
                                                         .expected(expectedData)
                                                         .build();
        updateRequestRepository.save(updateRequest);
        

        assertThat(requestRepository.count()).isEqualTo(1);
        Request request = requestRepository.findAll().get(0);
        assertThat(request.getExpected().getTime()).isEqualTo(Duration.ofHours(10));
        assertThat(request.getExpected().getDistance()).isEqualTo(20.0);
        assertThat(request.getExpected().getCost()).isEqualTo(30.0);
        assertThat(updateRequestRepository.count()).isEqualTo(1);
        

        final String reason = "Test message: 10635";
        var message = ApproveUpdateTripRequestMessage.builder()
                                                     .updateId(updateRequestRepository.findAll().get(0).getId())
                                                     .approved(false)
                                                     .approvedByEmployeeId(testEmployee1.getId())
                                                     .message(reason)
                                                     .build();
        
        approveUpdateTripInput.accept(MessageBuilder.withPayload(message).build());
        

        assertThat(requestRepository.count()).isEqualTo(1);
        request = requestRepository.findAll().get(0);
        assertThat(request.getExpected().getTime()).isEqualTo(Duration.ofHours(10));
        assertThat(request.getExpected().getDistance()).isEqualTo(20.0);
        assertThat(request.getExpected().getCost()).isEqualTo(30.0);
        assertThat(updateRequestRepository.count()).isZero();
    }
    
    private TripPurpose createTripPurpose() {
        var purpose = TripPurpose.builder().purpose("Purpose").id(UUID.randomUUID()).build();
        return tripPurposeRepository.save(purpose);
    }
    
}
