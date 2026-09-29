package ru.sberbank.ditsib.transport.request.messaging.listeners;

import io.qameta.allure.Feature;
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
import org.springframework.test.context.jdbc.Sql;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.integrations.carsharing.messages.CarsharingDataMessage;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingTripRepository;
import ru.sberbank.ditsib.transport.request.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForCarsharingRepository;
import ru.sberbank.ditsib.transport.request.mappers.CarsharingTripMapper;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Тест слушателя запросов на поездку через каршеринг")
class CarsharingTripListenerImplTest extends KafkaTest {

    @Autowired
    private CarsharingTripRepository repository;
    @Autowired
    private RequestForCarsharingRepository requestForCarsharingRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private CarsharingTripMapper mapper;
    @Autowired
    private RequestService service;
    @Autowired
    @Qualifier("carsharingTripInput")
    private Consumer<Message<CarsharingDataMessage>> carsharingTripInput;

    @Test
    @DisplayName("Тест получения сообщений с разными статусами заявки на поездку")
    @Sql(scripts = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/request_for_carsharing.sql",
    })
    void accept() {
        var message = getMessage("81bf3e90-0e53-4da4-b53e-50c68f77c639", 1, null);
        //создаем carsharing trip с rentId = 1
        carsharingTripInput.accept(MessageBuilder.withPayload(message).build());

        var savedTrip = repository.findFirstByRentId(1);
        assertThat(savedTrip).isPresent();
        assertThat(savedTrip.get().getRentId()).isEqualTo(1);
        var message2 = getMessage("71bf3e90-0e53-4da4-b53e-50c68f77c639", 2, null);
        //
        carsharingTripInput.accept(MessageBuilder.withPayload(message2).build());
        var inProgress = requestForCarsharingRepository.findById(UUID.fromString("71bf3e90-0e53-4da4-b53e-50c68f77c639"));
        assertThat(inProgress).isPresent();
        assertThat(inProgress.get().getStatus()).isEqualTo(TripRequestStatus.CARSHARING_TRIP_IN_PROGRESS);

        var message3 = getMessage("41bf3e90-0e53-4da4-b53e-50c68f77c639", 3, LocalDateTime.now());
        carsharingTripInput.accept(MessageBuilder.withPayload(message3).build());
        var finished = requestForCarsharingRepository.findById(UUID.fromString("41bf3e90-0e53-4da4-b53e-50c68f77c639"));
        assertThat(finished).isPresent();
        assertThat(finished.get().getStatus()).isEqualTo(TripRequestStatus.CARSHARING_TRIP_FINISHED);
    }

    private CarsharingDataMessage getMessage(String id, int rentId, LocalDateTime finishedAt) {
        return CarsharingDataMessage.builder()
                .id(UUID.fromString(id))
                .rentId(rentId)
                .rentCreatedAt(LocalDateTime.now().minusHours(1))
                .rentFinishedAt(finishedAt)
                .phoneNumber("+73123123123")
                .carModel("Model")
                .carNumber("Л333ЛЛ33")
                .drivingLength(22)
                .drivingLengthCost(500.0)
                .parkingTime(10)
                .parkingTimeCost(500.0)
                .drivingTime(30)
                .reserveTime(10)
                .reserveTimeCost(50.0)
                .drivingTimeCost(400.0)
                .eventStoredAt(LocalDateTime.now())
                .finishAddress("125319, г. Москва, пр-т Мира, д. 100")
                .startAddress("107045, г. Москва, ул. Ленина, д. 1")
                .startPoint(CarsharingDataMessage.Point.builder().latitude(55.706621).longitude(37.935297).build())
                .finishPoint(CarsharingDataMessage.Point.builder().latitude(55.706629).longitude(37.935299).build())
                .totalCost(4600.0)
                .build();
    }
}