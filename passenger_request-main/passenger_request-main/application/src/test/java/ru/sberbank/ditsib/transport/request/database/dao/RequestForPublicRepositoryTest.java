package ru.sberbank.ditsib.transport.request.database.dao;

import io.qameta.allure.Feature;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка репозитория заявок на ОТ")
@RequiredArgsConstructor
class RequestForPublicRepositoryTest extends KafkaTest {

    @Autowired
    private RequestForPublicRepository repository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void findRequestForPublicWithPaymentDoneDeadlineViolation() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 16, 12, 0, 0);
        LocalDateTime desiredDate = LocalDateTime.of(2024, 3, 1, 11, 0, 0);

        var employee = employeeRepository.save(Employee.builder()
                .id(UUID.randomUUID())
                .firstName("firstName")
                .lastName("lastName")
                .build());

        var testRequest1 = RequestForPublic.builder()
                .transportType(TransportTypeEnum.PUBLIC)
                .humanReadableId("testRequest1")
                .status(TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION)
                .author(employee)
                .passenger(employee)
                .desiredDate(desiredDate)
                .paymentDoneDeadline(now.minusHours(1))
                .build();

        repository.save(testRequest1);

        var testRequest2 = RequestForPublic.builder()
                .transportType(TransportTypeEnum.PUBLIC)
                .humanReadableId("testRequest2")
                .status(TripRequestStatus.PUBLIC_PAYMENT_AWAITING)
                .author(employee)
                .passenger(employee)
                .desiredDate(desiredDate)
                .paymentDoneDeadline(now.minusHours(1))
                .build();
        repository.save(testRequest2);

        var testRequest3 = RequestForPublic.builder()
                .transportType(TransportTypeEnum.PUBLIC)
                .humanReadableId("testRequest3")
                .status(TripRequestStatus.PUBLIC_APPROVED)
                .author(employee)
                .passenger(employee)
                .desiredDate(desiredDate)
                .paymentDoneDeadline(now.minusHours(1))
                .build();
        repository.save(testRequest3);

        var testRequest4 = RequestForPublic.builder()
                .humanReadableId("testRequest4")
                .status(TripRequestStatus.PUBLIC_PAYMENT_AWAITING)
                .transportType(TransportTypeEnum.PUBLIC)
                .author(employee)
                .passenger(employee)
                .desiredDate(desiredDate)
                .paymentDoneDeadline(now.plusHours(1))
                .build();
        repository.save(testRequest4);

        var result = repository.findRequestForPublicForSettingPaymentDoneDeadlineViolation(now,
                TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION,
                TripRequestStatus.PUBLIC_PAYMENT_AWAITING,
                DeadlineState.RED);

        assertEquals(2, result.size(),
                "Только заявки со статусом PUBLIC_ORDER_PAYMENT_FORMATION или PUBLIC_PAYMENT_AWAITING, у которых " +
                        "paymentDoneDeadline < текущего времени (now) считаются с нарушенным КС на выплату компенсации");
    }
}