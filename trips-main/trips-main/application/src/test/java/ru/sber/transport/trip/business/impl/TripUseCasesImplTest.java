package ru.sber.transport.trip.business.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.trip.business.TripUseCases;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.business.providers.SrmProvider;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.messaging.providers.AutoparkProvider;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.providers.ShiftProvider;
import ru.sber.transport.trip.messaging.senders.TripSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.ShiftSender;
import ru.sber.transport.trip.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trip.providers.trips.mapping.TripMapper;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@DisplayName("Проверка бизнес-кейсов поездок")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
class TripUseCasesImplTest {

    private final TripProvider tripProvider = mock(TripProvider.class);

    private final SrmProvider srmProvider = mock(SrmProvider.class);

    private final TripSender tripSender = mock(TripSender.class);

    private final DriverProvider driverProvider = mock(DriverProvider.class);

    private final ShiftProvider shiftProvider = mock(ShiftProvider.class);

    private final DriverSender driverSender = mock(DriverSender.class);

    private final ShiftSender shiftSender = mock(ShiftSender.class);

    private final ru.sber.transport.trip.messaging.senders.dispatcher.TripSender tripDispatcherSender = mock(ru.sber.transport.trip.messaging.senders.dispatcher.TripSender.class);

    private final TripHistoryProvider tripHistoryProvider = mock(TripHistoryProvider.class);

    private final TripMapper tripMapper = mock(TripMapper.class);

    private final ContractorProvider contractorProvider = mock(ContractorProvider.class);

    private final AutoparkProvider autoparkProvider = mock(AutoparkProvider.class);

    private final TripUseCases<Request> tripUseCases = new TripUseCasesImpl(tripProvider, srmProvider, tripSender, driverProvider, shiftProvider, driverSender, shiftSender, tripDispatcherSender, tripHistoryProvider, tripMapper, contractorProvider, autoparkProvider);

    @Test
    @DisplayName("Проверка новой индивидуальной поездки")
    void test_process_single_new() {
        var request = new Request();
        request.setId(UUID.randomUUID());
        request.setAuthorId(UUID.randomUUID());
        request.setPassengerId(UUID.randomUUID());
        request.setTaxiClass(TaxiClass.ECONOMY.name());
        request.setPassengerCount(1);
        request.setExpected(new ExpectedData(2, 3, Duration.ofSeconds(4)));
        request.setCreationTime(OffsetDateTime.now());
        request.setDesiredDate(OffsetDateTime.now());
        request.setRideId(null);
        request.setCoopTrip(false);
        request.setSuburb(true);
        request.setTimeZone("TZ");
        request.setRequestOptions(List.of("String"));
        request.setTariffId(UUID.randomUUID());
        request.setContractorId(UUID.randomUUID());
        request.setStatus(TripRequestStatus.TAXI_AWAITING_SEARCH.name());
        request.setComment("Comment");
        request.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 5.5, 6.6, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                null,
                null, null)));

//        var future = new CompletableFuture<Integer>();
        when(tripProvider.save(any(Trip.class))).thenReturn(1);

        tripUseCases.process(request, null, null);
//        future.complete(1);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        var finalCaptor = tripCaptor;
        await().pollDelay(Duration.ofSeconds(1))
                .timeout(Duration.ofSeconds(30))
                .until(() -> {
                    verify(tripProvider).save(finalCaptor.capture());
                    return finalCaptor.getValue() != null;
                });
        verify(tripProvider).save(tripCaptor.capture());

        var actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getExpectedEndTime()).isEqualTo(request.getDesiredDate().plus(request.getExpected().time()));
        assertThat(actual.getExpectedStartTime()).isEqualTo(request.getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.WAITING_FOR_ASSIGNMENT);
        assertThat(actual.getWaypoints()).isEqualTo(request.getWaypoints());
        assertThat(actual.getPassengerCount()).isEqualTo(request.getPassengerCount());
        assertThat(actual.getTaxiClass()).isEqualTo(request.getTaxiClass());
        assertThat(actual.getContractorId()).isEqualTo(request.getContractorId());

        tripCaptor = ArgumentCaptor.forClass(Trip.class);

        verify(tripSender).send(tripCaptor.capture());

        actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getExpectedEndTime()).isEqualTo(request.getDesiredDate().plus(request.getExpected().time()));
        assertThat(actual.getExpectedStartTime()).isEqualTo(request.getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.WAITING_FOR_ASSIGNMENT);
        assertThat(actual.getWaypoints()).isEqualTo(request.getWaypoints());
        assertThat(actual.getPassengerCount()).isEqualTo(request.getPassengerCount());
        assertThat(actual.getTaxiClass()).isEqualTo(request.getTaxiClass());
        assertThat(actual.getContractorId()).isEqualTo(request.getContractorId());
    }

    @Test
    @DisplayName("Проверка новой индивидуальной поездки (групповой трансфер)")
    void test_process_single_new_group_transfer() {
        var request = new Request();
        request.setId(UUID.randomUUID());
        request.setAuthorId(UUID.randomUUID());
        request.setPassengerId(UUID.randomUUID());
        request.setTaxiClass(null);
        request.setPassengerCount(1);
        request.setExpected(new ExpectedData(2, 3, Duration.ofSeconds(4)));
        request.setCreationTime(OffsetDateTime.now());
        request.setDesiredDate(OffsetDateTime.now());
        request.setRideId(null);
        request.setCoopTrip(false);
        request.setSuburb(true);
        request.setTimeZone("TZ");
        request.setRequestOptions(List.of("String"));
        request.setTariffId(UUID.randomUUID());
        request.setContractorId(UUID.randomUUID());
        request.setStatus(TripRequestStatus.TAXI_AWAITING_SEARCH.name());
        request.setComment("Comment");
        request.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 5.5, 6.6, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                null,
                null, null)));
        request.setTransportType("GROUP_TRANSFER");

//        var future = new CompletableFuture<Integer>();
        when(tripProvider.save(any(Trip.class))).thenReturn(1);

        tripUseCases.process(request, null, null);
//        future.complete(1);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        var finalCaptor = tripCaptor;
        await().pollDelay(Duration.ofSeconds(1))
                .timeout(Duration.ofSeconds(30))
                .until(() -> {
                    verify(tripProvider).save(finalCaptor.capture());
                    return finalCaptor.getValue() != null;
                });
        verify(tripProvider).save(tripCaptor.capture());

        var actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getExpectedEndTime()).isEqualTo(request.getDesiredDate().plus(request.getExpected().time()));
        assertThat(actual.getExpectedStartTime()).isEqualTo(request.getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.WAITING_FOR_ASSIGNMENT);
        assertThat(actual.getWaypoints()).isEqualTo(request.getWaypoints());
        assertThat(actual.getPassengerCount()).isEqualTo(request.getPassengerCount());
        assertThat(actual.getTaxiClass()).isEqualTo(request.getTransportType());
        assertThat(actual.getContractorId()).isEqualTo(request.getContractorId());

        tripCaptor = ArgumentCaptor.forClass(Trip.class);

        verify(tripSender).send(tripCaptor.capture());

        actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getExpectedEndTime()).isEqualTo(request.getDesiredDate().plus(request.getExpected().time()));
        assertThat(actual.getExpectedStartTime()).isEqualTo(request.getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.WAITING_FOR_ASSIGNMENT);
        assertThat(actual.getWaypoints()).isEqualTo(request.getWaypoints());
        assertThat(actual.getPassengerCount()).isEqualTo(request.getPassengerCount());
        assertThat(actual.getTaxiClass()).isEqualTo(request.getTransportType());
        assertThat(actual.getContractorId()).isEqualTo(request.getContractorId());
    }

    @Test
    @DisplayName("Проверка новой совместной поездки")
    void test_process_coop_new() {
        var request = new Request();
        request.setId(UUID.randomUUID());
        request.setAuthorId(UUID.randomUUID());
        request.setPassengerId(UUID.randomUUID());
        request.setTaxiClass(TaxiClass.ECONOMY.name());
        request.setPassengerCount(1);
        request.setExpected(new ExpectedData(2, 3, Duration.ofSeconds(4)));
        request.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        request.setDesiredDate(OffsetDateTime.now(ZoneOffset.UTC));
        request.setRideId(UUID.randomUUID());
        request.setCoopTrip(true);
        request.setSuburb(true);
        request.setTimeZone("TZ");
        request.setRequestOptions(List.of("String"));
        request.setTariffId(UUID.randomUUID());
        request.setContractorId(UUID.randomUUID());
        request.setStatus(TripRequestStatus.TAXI_AWAITING_SEARCH.name());
        request.setComment("Comment");
        request.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 5.5, 6.6, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                null,
                null, null)));

        var future = new CompletableFuture<Integer>();
        when(tripProvider.save(any(Trip.class))).thenReturn(1);
        when(srmProvider.update(any(Trip.class))).thenAnswer(inv -> {
            var f = new CompletableFuture<>();
            f.complete(inv.getArgument(0, Trip.class));
            return f;
        });

        tripUseCases.process(request, null, null);
        future.complete(1);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        await().pollDelay(Duration.ofSeconds(1))
                .timeout(Duration.ofSeconds(30))
                .until(() -> {
                    verify(tripProvider).save(tripCaptor.capture());
                    return tripCaptor.getValue() != null;
                });
        verify(srmProvider, never()).update(any());
    }

    @Test
    @DisplayName("Проверка присоединения к совместной поездке")
    void test_process_coop_join() {
        var request = Instancio.of(Request.class)
                .set(Select.field(Request::getStatus), TripRequestStatus.TAXI_AWAITING_SEARCH.name())
                .set(Select.field(Request::isCoopTrip), true)
                .create();

        var trip = Instancio.of(Trip.class)
                .set(Select.field(Trip::getRequests), new HashSet<>(Set.of(Instancio.of(Request.class)
                        .set(Select.field(Request::getStatus), TripRequestStatus.TAXI_AWAITING_SEARCH.name())
                        .set(Select.field(Request::getRideId), request.getRideId())
                        .create())))
                .set(Select.field(Trip::getStatus), TripStatus.getByTripRequestStatus(TripRequestStatus.TAXI_AWAITING_SEARCH))
                .create();

        var future = new CompletableFuture<Integer>();
        when(tripProvider.get(request.getRideId())).thenReturn(Optional.of(trip));
        when(tripProvider.save(any(Trip.class))).thenReturn(1);
        when(srmProvider.update(any(Trip.class))).thenAnswer(inv -> {
            var f = new CompletableFuture<>();
            f.complete(inv.getArgument(0, Trip.class));
            return f;
        });

        tripUseCases.process(request, null, null);
        future.complete(1);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        await().pollDelay(Duration.ofSeconds(1))
                .timeout(Duration.ofSeconds(30))
                .until(() -> {
                    verify(tripProvider).save(tripCaptor.capture());
                    return tripCaptor.getValue() != null;
                });

        verify(srmProvider).update(tripCaptor.capture());

        var actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getRideId());
        assertThat(actual.getTaxiClass()).isEqualTo(request.getTaxiClass());
        assertThat(actual.getContractorId()).isEqualTo(request.getContractorId());
    }

    @Test
    @DisplayName("Проверка присоединения к совместной поездке в неприсоединяемом статусе")
    void test_process_coop_join_unprocessable() {
        var request = Instancio.of(Request.class)
                .set(Select.field(Request::getStatus), TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name())
                .set(Select.field(Request::isCoopTrip), true)
                .create();

        var trip = Instancio.of(Trip.class)
                .set(Select.field(Trip::getRequests), new HashSet<>(Set.of(Instancio.of(Request.class)
                        .set(Select.field(Request::getStatus), TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name())
                        .set(Select.field(Request::getRideId), request.getRideId())
                        .create())))
                .set(Select.field(Trip::getStatus), TripStatus.getByTripRequestStatus(TripRequestStatus.TAXI_TRIP_IN_PROGRESS))
                .create();

        var future = new CompletableFuture<Integer>();
        when(tripProvider.get(request.getRideId())).thenReturn(Optional.of(trip));
        when(tripProvider.save(any(Trip.class))).thenReturn(1);
        when(srmProvider.update(any(Trip.class))).thenAnswer(inv -> {
            var f = new CompletableFuture<>();
            f.complete(inv.getArgument(0, Trip.class));
            return f;
        });

        tripUseCases.process(request, null, null);
        future.complete(1);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        await().pollDelay(Duration.ofSeconds(1))
                .timeout(Duration.ofSeconds(30))
                .until(() -> {
                    verify(tripProvider).save(tripCaptor.capture());
                    return tripCaptor.getValue() != null;
                });

        verify(srmProvider, never()).update(any());
        verify(tripProvider).save(tripCaptor.capture());

        assertThat(tripCaptor.getValue().getStatus()).isEqualTo(TripStatus.getByTripRequestStatus(TripRequestStatus.valueOf(request.getStatus())));
    }

    @Test
    @DisplayName("Проверка измененной индивидуальной поездки")
    void test_process_single_edited() {
        var trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setContractorId(UUID.randomUUID());
        trip.setPassengerCount(3);
        trip.setTaxiClass(TaxiClass.ECONOMY.name());
        trip.setDriverWaitingTime(Duration.ZERO);
        trip.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 1.1, 2.2, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                null,
                null, null)));
        trip.setExpectedEndTime(OffsetDateTime.now().minusDays(1));
        trip.setExpectedStartTime(OffsetDateTime.now().minusDays(2));
        trip.setStatus(TripStatus.TRIP_IN_PROGRESS);

        var request = new Request();
        request.setId(trip.getId());
        request.setAuthorId(UUID.randomUUID());
        request.setPassengerId(UUID.randomUUID());
        request.setTaxiClass(TaxiClass.ECONOMY.name());
        request.setPassengerCount(1);
        request.setExpected(new ExpectedData(2, 3, Duration.ofSeconds(4)));
        request.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        request.setDesiredDate(OffsetDateTime.now(ZoneOffset.UTC));
        request.setRideId(null);
        request.setCoopTrip(false);
        request.setSuburb(true);
        request.setTimeZone("TZ");
        request.setRequestOptions(List.of("String"));
        request.setTariffId(UUID.randomUUID());
        request.setContractorId(UUID.randomUUID());
        request.setStatus("TAXI_CANCELLED");
        request.setComment("Comment");
        request.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 5.5, 6.6, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                null,
                null, null)));

        when(tripProvider.get(request.getId())).thenReturn(Optional.of(trip));

        var future = new CompletableFuture<Integer>();
        when(tripProvider.save(any(Trip.class))).thenReturn(1);
        when(srmProvider.update(any(Trip.class))).thenAnswer(inv -> {
            var f = new CompletableFuture<>();
            f.complete(inv.getArgument(0, Trip.class));
            return f;
        });

        tripUseCases.process(request, null, null);
        future.complete(1);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        await()
                .pollDelay(Duration.ofSeconds(1))
                .timeout(Duration.ofSeconds(30))
                .until(() -> {
                    verify(tripProvider).save(tripCaptor.capture());
                    return tripCaptor.getValue() != null;
                });

        var actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getExpectedEndTime()).isEqualTo(request.getDesiredDate().plus(request.getExpected().time()));
        assertThat(actual.getExpectedStartTime()).isEqualTo(request.getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.ORDER_CANCELLED_BY_CLIENT);
        assertThat(actual.getWaypoints()).isEqualTo(request.getWaypoints());
        assertThat(actual.getPassengerCount()).isEqualTo(request.getPassengerCount());
        assertThat(actual.getTaxiClass()).isEqualTo(request.getTaxiClass());
        assertThat(actual.getContractorId()).isEqualTo(request.getContractorId());
    }

    @Test
    @DisplayName("Проверка завершения индивидуальной поездки")
    void test_process_coop_edited() {
        var trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setContractorId(UUID.randomUUID());
        trip.setPassengerCount(3);
        trip.setTaxiClass(TaxiClass.ECONOMY.name());
        trip.setDriverWaitingTime(Duration.ZERO);
        trip.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 1.1, 2.2, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                null,
                null, null)));
        trip.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        trip.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(2));
        trip.setStatus(TripStatus.TRIP_IN_PROGRESS);

        var request = new Request();
        request.setId(trip.getId());
        request.setAuthorId(UUID.randomUUID());
        request.setPassengerId(UUID.randomUUID());
        request.setTaxiClass(TaxiClass.ECONOMY.name());
        request.setPassengerCount(1);
        request.setExpected(new ExpectedData(2, 3, Duration.ofSeconds(4)));
        request.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        request.setDesiredDate(OffsetDateTime.now(ZoneOffset.UTC));
        request.setRideId(null);
        request.setCoopTrip(false);
        request.setSuburb(true);
        request.setTimeZone("TZ");
        request.setRequestOptions(List.of("String"));
        request.setTariffId(UUID.randomUUID());
        request.setContractorId(UUID.randomUUID());
        request.setStatus("TAXI_TRIP_FINISHED");
        request.setComment("Comment");
        request.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 5.5, 6.6, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                null,
                null, null)));

        when(tripProvider.get(request.getId())).thenReturn(Optional.of(trip));

        var future = new CompletableFuture<Integer>();
        when(tripProvider.save(any(Trip.class))).thenReturn(1);
        when(srmProvider.update(any(Trip.class))).thenAnswer(inv -> {
            var f = new CompletableFuture<>();
            f.complete(inv.getArgument(0, Trip.class));
            return f;
        });

        tripUseCases.process(request, null, null);
        future.complete(1);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        await()
                .pollDelay(Duration.ofSeconds(1))
                .timeout(Duration.ofSeconds(30))
                .until(() -> {
                    verify(tripProvider).save(tripCaptor.capture());
                    return tripCaptor.getValue() != null;
                });

        var actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getExpectedEndTime()).isEqualTo(request.getDesiredDate().plus(request.getExpected().time()));
        assertThat(actual.getExpectedStartTime()).isEqualTo(request.getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.ORDER_FINISHED);
        assertThat(actual.getWaypoints()).isEqualTo(request.getWaypoints());
        assertThat(actual.getPassengerCount()).isEqualTo(request.getPassengerCount());
        assertThat(actual.getTaxiClass()).isEqualTo(request.getTaxiClass());
        assertThat(actual.getContractorId()).isEqualTo(request.getContractorId());
    }

    @Test
    @DisplayName("Проверка неуспешной отмены индивидуальной поездки")
    void test_process_coop_cancel_unsuccessfully() {
        var trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setContractorId(UUID.randomUUID());
        trip.setPassengerCount(3);
        trip.setTaxiClass(TaxiClass.ECONOMY.name());
        trip.setDriverWaitingTime(Duration.ZERO);
        trip.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 1.1, 2.2, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                null,
                null, null)));
        trip.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        trip.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusDays(2));
        trip.setStatus(TripStatus.ORDER_CANCELLED_BY_CLIENT);

        var request = new Request();
        request.setId(trip.getId());
        request.setAuthorId(UUID.randomUUID());
        request.setPassengerId(UUID.randomUUID());
        request.setTaxiClass(TaxiClass.ECONOMY.name());
        request.setPassengerCount(1);
        request.setExpected(new ExpectedData(2, 3, Duration.ofSeconds(4)));
        request.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        request.setDesiredDate(OffsetDateTime.now(ZoneOffset.UTC));
        request.setRideId(null);
        request.setCoopTrip(false);
        request.setSuburb(true);
        request.setTimeZone("TZ");
        request.setRequestOptions(List.of("String"));
        request.setTariffId(UUID.randomUUID());
        request.setContractorId(UUID.randomUUID());
        request.setStatus("TAXI_CANCELLED");
        request.setComment("Comment");
        request.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 5.5, 6.6, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                null,
                null, null)));

        when(tripProvider.get(request.getId())).thenReturn(Optional.of(trip));

        var future = new CompletableFuture<Integer>();
        when(tripProvider.save(any(Trip.class))).thenReturn(1);
        when(srmProvider.update(any(Trip.class))).thenAnswer(inv -> {
            var f = new CompletableFuture<>();
            f.complete(inv.getArgument(0, Trip.class));
            return f;
        });

        tripUseCases.process(request, null, null);
        future.complete(1);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        await()
                .pollDelay(Duration.ofSeconds(1))
                .timeout(Duration.ofSeconds(30))
                .until(() -> {
                    verify(tripProvider).save(tripCaptor.capture());
                    return tripCaptor.getValue() != null;
                });

        var actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getExpectedEndTime()).isEqualTo(request.getDesiredDate().plus(request.getExpected().time()));
        assertThat(actual.getExpectedStartTime()).isEqualTo(request.getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.ORDER_CANCELLED_BY_CLIENT);
        assertThat(actual.getWaypoints()).isEqualTo(request.getWaypoints());
        assertThat(actual.getPassengerCount()).isEqualTo(request.getPassengerCount());
        assertThat(actual.getTaxiClass()).isEqualTo(request.getTaxiClass());
        assertThat(actual.getContractorId()).isEqualTo(request.getContractorId());
    }
}