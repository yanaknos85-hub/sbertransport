package ru.sber.transport.trips.cargo.providers.trips;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trips.cargo.business.model.CargoRequest;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.business.model.Waypoint;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.providers.trips.mapping.TripMapper;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.TripsRecord;

import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DisplayName("Проверка провайдера поездок")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_cargo")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class TripProviderImplTest extends KafkaTest {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private TripMapper tripMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContractorProvider contractorProvider;

    @DisplayName("Получение количества трипов")
    @Test
    void test_getCount() throws JsonProcessingException {
        var contractorId = UUID.randomUUID();

        createTrip(1, TripStatus.SENT_TO_CONTRACTOR, contractorId, null);
        createTrip(2, TripStatus.WAITING_FOR_ASSIGNMENT, contractorId, null);
        createTrip(3, TripStatus.WAITING_FOR_ASSIGNMENT, contractorId, null);
        createTrip(4, TripStatus.DRIVER_ASSIGNED, contractorId, null);

        var provider = new TripProviderImpl(dslContext, tripMapper, contractorProvider, new ObjectMapper());
        var count = provider.countByContractorIdAndStatusIn(contractorId, null,
                List.of(TripStatus.WAITING_FOR_ASSIGNMENT, TripStatus.DRIVER_ASSIGNED));

        assertEquals(3, count);
    }

    @DisplayName("Получение количества трипов")
    @Test
    void test_getCount_autoparkId() throws JsonProcessingException {
        var contractorId = UUID.randomUUID();

        var autoparkId = UUID.randomUUID();
        createTrip(1, TripStatus.SENT_TO_CONTRACTOR, contractorId, autoparkId);
        createTrip(2, TripStatus.WAITING_FOR_ASSIGNMENT, contractorId, autoparkId);
        createTrip(3, TripStatus.WAITING_FOR_ASSIGNMENT, contractorId, null);
        createTrip(4, TripStatus.DRIVER_ASSIGNED, contractorId, null);

        var provider = new TripProviderImpl(dslContext, tripMapper, contractorProvider, new ObjectMapper());
        var count = provider.countByContractorIdAndStatusIn(contractorId, autoparkId,
                List.of(TripStatus.WAITING_FOR_ASSIGNMENT, TripStatus.DRIVER_ASSIGNED));

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() throws JsonProcessingException {
        var request = createRequest(UUID.randomUUID());
        var request2 = createRequest(UUID.randomUUID());
        var request3 = createRequest(request.getId());

        Duration driverWaitingTime = generateDriverWaitingTime();
        var dispatcherStartTime = OffsetDateTime.parse("2024-06-04T00:00:00+03:00",
                DateTimeFormatter.ISO_DATE_TIME).plusDays(1);

        var provider = new TripProviderImpl(dslContext, tripMapper, contractorProvider, new ObjectMapper());

        var set = new HashSet<CargoRequest>();
        set.add(request);
        set.add(request2);
        set.add(request3);

        var trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setCapacity(2.0);
        trip.setStatus(TripStatus.TRIP_IN_PROGRESS);
        trip.setEndTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC).plusDays(1), ZoneOffset.UTC));
        trip.setStartTime(OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC));
        trip.setContractorId(UUID.randomUUID());
        trip.setRequests(set);
        trip.setDriverWaitingTime(driverWaitingTime);
        trip.setDispatcherStartTime(dispatcherStartTime);

        provider.save(trip);

        var actual = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(trip.getId())).fetchSingle();

        assertThat(actual.getId()).isEqualTo(trip.getId());
        assertThat(actual.getContractorId()).isEqualTo(trip.getContractorId());
        assertThat(actual.getCapacity()).isEqualTo(trip.getCapacity());
        assertThat(actual.getStatus()).isEqualTo(trip.getStatus().name());
        assertDates(actual.getStartTime(), trip.getStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())));
        assertDates(actual.getEndTime(), trip.getEndTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())));
        assertThat(objectMapper.readValue(actual.getRequests().data(), new TypeReference<List<CargoRequest>>() {})).hasSameElementsAs(Set.of(request, request2));
        assertThat(actual.getDriverWaitingTime()).isEqualTo(driverWaitingTime.toMillis());
        assertThat(actual.getDispatcherStartTime()).isEqualTo(dispatcherStartTime);
        assertThat(actual.getLoaders()).isZero();
    }

    private void assertDates(OffsetDateTime actual, OffsetDateTime expected) {
        assertThat(actual.getYear()).isEqualTo(expected.getYear());
        assertThat(actual.getMonth()).isEqualTo(expected.getMonth());
        assertThat(actual.getDayOfMonth()).isEqualTo(expected.getDayOfMonth());
        assertThat(actual.getHour()).isEqualTo(expected.getHour());
        assertThat(actual.getMinute()).isEqualTo(expected.getMinute());
        assertThat(actual.getSecond()).isEqualTo(expected.getSecond());
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
    private Duration generateDriverWaitingTime() {
        Random random = new Random();
        long randomSeconds = random.nextInt(3600);
        var driverWaitingTime = Duration.ofSeconds(randomSeconds);
        return driverWaitingTime;
    }

}