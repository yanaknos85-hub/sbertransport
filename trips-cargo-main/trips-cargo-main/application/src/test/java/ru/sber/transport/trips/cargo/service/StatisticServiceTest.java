package ru.sber.transport.trips.cargo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trips.cargo.business.model.CargoRequest;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.web.service.StatisticService;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.TripsRecord;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка сервиса статистики")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
public class StatisticServiceTest extends KafkaTest {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private StatisticService statisticService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void createData() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @DisplayName("Проверка получения статистики по назначению трипов")
    @Test
    void test_getStatistic() throws JsonProcessingException {
        var contractorId = UUID.randomUUID();
        var autoparkId = UUID.randomUUID();

        createTrip(1, TripStatus.SENT_TO_CONTRACTOR, contractorId, autoparkId);
        createTrip(2, TripStatus.WAITING_FOR_ASSIGNMENT, contractorId, autoparkId);
        createTrip(3, TripStatus.DRIVER_ASSIGNED, contractorId, autoparkId);
        createTrip(5, TripStatus.DRIVER_ON_THE_WAY, contractorId, autoparkId);
        createTrip(6, TripStatus.DRIVER_ARRIVED, contractorId, autoparkId);
        createTrip(7, TripStatus.TRIP_IN_PROGRESS, contractorId, autoparkId);
        createTrip(8, TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, contractorId, autoparkId);
        createTrip(9, TripStatus.ORDER_CANCELLED_BY_CLIENT, contractorId, autoparkId);

        var dto = statisticService.getAssignStatistic(contractorId, autoparkId);

        assertEquals(7, dto.getTotalCount());
        assertEquals(5, dto.getAssignCount());
        assertEquals(2, dto.getNotAssignCount());
    }

    private void createTrip(int index, TripStatus status, UUID contractorId, UUID autoparkId) throws JsonProcessingException {
        var trip = new TripsRecord();
        trip.setId(UUID.randomUUID());
        trip.setDigitId(BigInteger.valueOf(index));
        trip.setContractorId(contractorId);
        trip.setAutoparkId(autoparkId);
        trip.setStatus(status.name());
        trip.setStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(index));
        trip.setRequests(JSON.json(objectMapper.writeValueAsString(Set.of(createRequest(UUID.randomUUID())))));
        trip.setWaypoints(JSON.json(objectMapper.writeValueAsString(List.of())));

        dslContext.insertInto(Tables.TRIPS).set(trip).execute();
    }

    private CargoRequest createRequest(UUID id) {
        var request = new CargoRequest();

        request.setTariffId(UUID.randomUUID());
        request.setId(id);

        return request;
    }
}
