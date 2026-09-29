package ru.sber.transport.notifications.messaging.listeners.impl;

import io.qameta.allure.Feature;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.approvals.messages.ApproveSharedRideMessage;
import ru.sber.transport.notifications.database.dao.messages.approve.TripApproveRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripRequestRepository;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EmbeddedPostgres
@Transactional
@DisplayName("Проверка слушателя сообщений утверждение совместной поездки")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class ApproveSharedRideHandlerImplTest {

    @Autowired
    private TripApproveRepository approveRepository;
    @Autowired
    private TripRequestRepository tripRequestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private Consumer<Message<ApproveSharedRideMessage>> sharedRideApproveInput;

    @DisplayName("Новый")
    @Test
    void test_new() {
    
        UUID uuidEmployee = UUID.randomUUID();
        var employee = Employee.builder().id(uuidEmployee).build();
        employeeRepository.save(employee);
        TripRequest tripRequest = new TripRequest();
        tripRequest.setId(UUID.randomUUID());
        tripRequest.setPassenger(employee);
        tripRequest.setPassengerId(uuidEmployee);
        tripRequestRepository.save(tripRequest);
        UUID tripRequestId = tripRequest.getId();
        TripRequest ownerTripRequest = new TripRequest();
        ownerTripRequest.setId(UUID.randomUUID());
        tripRequestRepository.save(tripRequest);
        UUID ownerTripRequestId = ownerTripRequest.getId();
        
        var message = new ApproveSharedRideMessage(
                tripRequestId,
                ownerTripRequestId,
                true,
                UUID.randomUUID(),
                null,
                "APPROVED",
                null
        );
    
        assertThat(approveRepository.count()).isZero();
        sharedRideApproveInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(approveRepository.count()).isEqualTo(1);
    }
}