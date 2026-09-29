package ru.sber.transport.trip.providers.trips;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.apache.commons.collections4.IteratorUtils;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.trip.business.dto.RequestSearchDto;
import ru.sber.transport.trip.business.dto.RequestSearchParameters;
import ru.sber.transport.trip.business.model.Request;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.business.model.Waypoint;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.TripsRecord;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.providers.trips.mapping.TripMapper;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.request.Direction;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.math.BigInteger;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DisplayName("Проверка провайдера поездок")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
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

    @DisplayName("Получение")
    @Test
    void test_get() throws JsonProcessingException {
        var id = UUID.randomUUID();
        var waypoints = List.of(
                new Waypoint(UUID.randomUUID(), 1.1, 2.2, 0,
                        "country",
                        "region",
                        "city",
                        "street",
                        "house",
                        "building",
                        Duration.ZERO,
                        null,
                        null, null)
        );

        var trip = new TripsRecord();
        trip.setId(id);
        trip.setDigitId(BigInteger.valueOf(2));
        trip.setTaxiClass(TaxiClass.ECONOMY.name());
        trip.setContractorId(UUID.randomUUID());
        trip.setStatus(TripStatus.SENT_TO_CONTRACTOR.name());
        trip.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setFactEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        trip.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        trip.setRequests(JSON.json(objectMapper.writeValueAsString(Set.of(createRequest(UUID.randomUUID())))));
        trip.setWaypoints(JSON.json(objectMapper.writeValueAsString(waypoints)));
        trip.setReportCreated(false);

        dslContext.insertInto(Tables.TRIPS_).set(trip).execute();

        var provider = new TripProviderImpl(dslContext, tripMapper, contractorProvider, new ObjectMapper());

        var actual = provider.get(id);

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(id);
        assertThat(actual.get().getContractorId()).isEqualTo(trip.getContractorId());
        assertThat(actual.get().getTaxiClass()).isEqualTo(trip.getTaxiClass());
        assertThat(actual.get().getStatus().name()).isEqualTo(trip.getStatus());
        assertDates(actual.get().getExpectedStartTime().withOffsetSameInstant(ZoneOffset.UTC), trip.getExpectedStartTime());
        assertDates(actual.get().getExpectedEndTime().withOffsetSameInstant(ZoneOffset.UTC), trip.getExpectedEndTime());
    }

    @DisplayName("Получение количества трипов")
    @Test
    void test_getCount() throws JsonProcessingException {
        var contractorId = UUID.randomUUID();

        createTrip(1, TripStatus.SENT_TO_CONTRACTOR, contractorId, null);
        createTrip(2, TripStatus.WAITING_FOR_ASSIGNMENT, contractorId, null);
        createTrip(3, TripStatus.WAITING_FOR_ASSIGNMENT, contractorId, null);
        createTrip(4, TripStatus.DRIVER_ASSIGNED, contractorId, null);

        var provider = new TripProviderImpl(dslContext, tripMapper, contractorProvider, new ObjectMapper());
        var count = provider.countByContractorIdAndStatusIn(contractorId,
                List.of(TripStatus.WAITING_FOR_ASSIGNMENT, TripStatus.DRIVER_ASSIGNED), null);

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
        var count = provider.countByContractorIdAndStatusIn(contractorId,
                List.of(TripStatus.WAITING_FOR_ASSIGNMENT, TripStatus.DRIVER_ASSIGNED), autoparkId);

        assertEquals(1, count);
    }

    @DisplayName("Получение. Сортировка по времени начала поездки")
    @Test
    void test_getSorted() throws JsonProcessingException {
        var contractorId = UUID.randomUUID();

        var waypoints = List.of(
                new Waypoint(UUID.randomUUID(), 1.1, 2.2, 0,
                        "country",
                        "region",
                        "city",
                        "street",
                        "house",
                        "building",
                        Duration.ZERO,
                        null,
                        null, null)
        );

        var trip1 = new TripsRecord();
        trip1.setId(UUID.randomUUID());
        trip1.setDigitId(BigInteger.valueOf(1));
        trip1.setTaxiClass(TaxiClass.ECONOMY.name());
        trip1.setContractorId(contractorId);
        trip1.setStatus(TripStatus.SENT_TO_CONTRACTOR.name());
        trip1.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(3));
        trip1.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        trip1.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(3));
        trip1.setFactEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        trip1.setRequests(JSON.json(objectMapper.writeValueAsString(Set.of(createRequest(UUID.randomUUID())))));
        trip1.setWaypoints(JSON.json(objectMapper.writeValueAsString(waypoints)));
        trip1.setReportCreated(false);

        var trip2 = new TripsRecord();
        trip2.setId(UUID.randomUUID());
        trip2.setDigitId(BigInteger.valueOf(2));
        trip2.setTaxiClass(TaxiClass.ECONOMY.name());
        trip2.setContractorId(contractorId);
        trip2.setStatus(TripStatus.SENT_TO_CONTRACTOR.name());
        trip2.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip2.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        trip2.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip2.setFactEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        trip2.setRequests(JSON.json(objectMapper.writeValueAsString(Set.of(createRequest(UUID.randomUUID())))));
        trip2.setWaypoints(JSON.json(objectMapper.writeValueAsString(waypoints)));
        trip2.setReportCreated(false);

        var trip3 = new TripsRecord();
        trip3.setId(UUID.randomUUID());
        trip3.setDigitId(BigInteger.valueOf(3));
        trip3.setTaxiClass(TaxiClass.ECONOMY.name());
        trip3.setContractorId(contractorId);
        trip3.setStatus(TripStatus.SENT_TO_CONTRACTOR.name());
        trip3.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        trip3.setFactEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        trip3.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        trip3.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        trip3.setRequests(JSON.json(objectMapper.writeValueAsString(Set.of(createRequest(UUID.randomUUID())))));
        trip3.setWaypoints(JSON.json(objectMapper.writeValueAsString(waypoints)));
        trip3.setReportCreated(false);

        dslContext.insertInto(Tables.TRIPS_).set(trip1).execute();
        dslContext.insertInto(Tables.TRIPS_).set(trip2).execute();
        dslContext.insertInto(Tables.TRIPS_).set(trip3).execute();

        var provider = new TripProviderImpl(dslContext, tripMapper, contractorProvider, new ObjectMapper());

        var searchDto = new RequestSearchDto();
        searchDto.setField(RequestSearchParameters.EXPECTED_START_TIME);
        searchDto.setDirection(Direction.DESC);
        searchDto.setPage(0);
        searchDto.setSize(10);

        var actual = IteratorUtils.toList(provider.findAll(
                contractorId, searchDto, List.of(TripStatus.SENT_TO_CONTRACTOR)
        ).iterator());

        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual.get(0).getId()).isEqualTo(trip2.getId());
        assertThat(actual.get(1).getId()).isEqualTo(trip3.getId());
        assertThat(actual.get(2).getId()).isEqualTo(trip1.getId());
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() throws JsonProcessingException {
        var request = createRequest(UUID.randomUUID());
        var request2 = createRequest(UUID.randomUUID());
        var request3 = createRequest(request.getId());

        var provider = new TripProviderImpl(dslContext, tripMapper, contractorProvider, new ObjectMapper());

        var set = new HashSet<Request>();
        set.add(request);
        set.add(request2);
        set.add(request3);

        var trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setTaxiClass(TaxiClass.ECONOMY.name());
        trip.setStatus(TripStatus.TRIP_IN_PROGRESS);
        trip.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));
        trip.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setContractorId(UUID.randomUUID());
        trip.setRequests(set);
        trip.setReportCreated(false);
        provider.save(trip);

//        var completed = provider.save(trip).toCompletableFuture();
//
//        await()
//                .pollInterval(Duration.ofSeconds(1))
//                .timeout(Duration.ofSeconds(30))
//                .until(completed::isDone);

        var actual = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(trip.getId())).fetchSingle();

            assertThat(actual.getId()).isEqualTo(trip.getId());
            assertThat(actual.getContractorId()).isEqualTo(trip.getContractorId());
            assertThat(actual.getTaxiClass()).isEqualTo(trip.getTaxiClass());
            assertThat(actual.getStatus()).isEqualTo(trip.getStatus().name());
            assertDates(actual.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.UTC), trip.getExpectedStartTime());
            assertDates(actual.getExpectedEndTime().withOffsetSameInstant(ZoneOffset.UTC), trip.getExpectedEndTime());
            assertThat(objectMapper.readValue(actual.getRequests().data(), new TypeReference<List<Request>>() {})).hasSameElementsAs(Set.of(request, request2));
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
        trip.setTaxiClass(TaxiClass.ECONOMY.name());
        trip.setContractorId(contractorId);
        trip.setStatus(status.name());
        trip.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(index));
        trip.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setFactEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(index));
        trip.setRequests(JSON.json(objectMapper.writeValueAsString(Set.of(createRequest(UUID.randomUUID())))));
        trip.setWaypoints(JSON.json(objectMapper.writeValueAsString(List.of())));
        trip.setReportCreated(false);
        trip.setAutoparkId(autoparkId);

        dslContext.insertInto(Tables.TRIPS_).set(trip).execute();
    }

    private Request createRequest(UUID id) {
        var request = new Request();

        request.setTariffId(UUID.randomUUID());
        request.setId(id);

        return request;
    }

}