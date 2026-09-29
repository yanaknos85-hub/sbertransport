package ru.sberbank.transport.oto.cargo.messaging;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.messaging.messages.trip.DeadlineStateUpdateMessage;
import ru.sberbank.transport.oto.cargo.SharedTestData;
import ru.sberbank.transport.oto.cargo.database.dao.*;
import ru.sberbank.transport.oto.cargo.database.model.Address;
import ru.sberbank.transport.oto.cargo.database.model.ExpectedData;
import ru.sberbank.transport.oto.cargo.database.model.Waypoint;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка получения данных об обновлении статуса КС")
@Disabled("Требует актуализации - падает в Jenkins")
@ActiveProfiles("test")
class DeadlineStateUpdateListenerTest extends SharedTestData {
    
    @Autowired
    private RequestRepository requestRepository;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    private final UUID REQUEST_ID = UUID.fromString("25742740-dd80-41fa-aaca-5e6db1029569");
    
    @AfterEach
    void clearRepository() {
        requestRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Обновление статуса КС")
    void updateDeadlineStateTest() {
        var contractor = createContractor("Contractor");
        var position = createPosition("Position");
        var department = createDepartment(UUID.randomUUID());
        var passenger = createPassenger(UUID.fromString(USER1_ID), "passenger-HRE-1",
                                        "Lennon", "John",
                                        "Johnovich", "8927",
                                        position, department);
        contractorRepository.save(contractor);
        positionRepository.save(position);
        departmentRepository.save(department);
        employeeRepository.save(passenger);
        
        List<Waypoint> waypoints = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            var address = addressRepository.save(Address.builder()
                                 .id(UUID.randomUUID())
                                 .region(String.format("Region%03d", i))
                                 .city(String.format("City%03d", i))
                                 .street(String.format("Street%03d", i))
                                 .house(String.format("House%03d", i))
                                 .build());
            waypoints.add(Waypoint.builder()
                                  .id(UUID.randomUUID())
                                  .address(address)
                                  .build());
        }
        var request = createRequest(REQUEST_ID,
                                    "request-HRE-1",
                                    TripRequestStatus.PERSONAL_PAYMENT_AWAITING,
                                    TransportTypeEnum.PERSONAL,
                                    contractor, passenger, ExpectedData.builder().cost(10d).distance(100d).build(),
                                    waypoints,
                                    LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).minusDays(1),
                                    LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                                    LocalDateTime.now());
        requestRepository.save(request);
    
        var message = new DeadlineStateUpdateMessage(REQUEST_ID, "RED", null);
        produceMessage("service.request.deadlineStateUpdate", MessageBuilder.withPayload(message).build());
        
        var actual = requestRepository.findById(REQUEST_ID).orElseThrow();
        assertThat(actual.getDeadlineState()).isEqualTo(DeadlineState.RED);
    }
}
