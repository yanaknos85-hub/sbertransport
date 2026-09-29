package ru.sber.transport.trips.cargo.business.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.trips.cargo.business.TripUseCases;
import ru.sber.transport.trips.cargo.business.mapper.TripUpdater;
import ru.sber.transport.trips.cargo.business.mapper.TripUpdaterImpl;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.providers.AutoparkProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ShiftProvider;
import ru.sber.transport.trips.cargo.messaging.senders.TripSender;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.ShiftSender;
import ru.sberbank.ditsib.transport.request.messaging.RouteMessage;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@DisplayName("Проверка бизнес-кейсов грузовых поездок")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_cargo")
class CargoTripUseCasesImplTest {

    private final TripProvider tripProvider = mock(TripProvider.class);

    private final TripSender tripSender = mock(TripSender.class);

    private final ContractorProvider contractorProvider = mock(ContractorProvider.class);

    private final ru.sber.transport.trips.cargo.messaging.senders.dispatcher.TripSender tripDispatcherSender =
            mock(ru.sber.transport.trips.cargo.messaging.senders.dispatcher.TripSender.class);

    private final DriverProvider driverProvider = mock(DriverProvider.class);

    private final ShiftProvider shiftProvider = mock(ShiftProvider.class);

    private final ShiftSender shiftSender = mock(ShiftSender.class);

    private final AutoparkProvider autoparkProvider = mock(AutoparkProvider.class);

    private final TripUpdater tripUpdater = new TripUpdaterImpl() {
        @Override
        public ObjectMapper objectMapper() {
            var mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper;
        }
    };

    private final TripUseCases<RouteMessage> tripUseCases = new CargoTripUseCasesImpl(tripProvider, tripSender,
            tripUpdater, contractorProvider, tripDispatcherSender, driverProvider, shiftProvider, shiftSender,
            autoparkProvider
    );

    @Test
    @DisplayName("Проверка маршрутного листа")
    void test_process_cargo() {
        var request = Instancio.of(RouteMessage.class)
                .set(field(RouteMessage::status), "CARGO_AWAITING_DATA")
                .set(field(RouteMessage::timeZone), "+3")
                .create();

        var loaders = request.requests().stream()
                .peek(r -> r.setTimeZone("+3"))
                .flatMapToInt(r -> IntStream.of(r.getSourceLoaders(), r.getDestinationLoaders()))
                .max()
                .orElse(0);

        var future = new CompletableFuture<Integer>();
        when(tripProvider.save(any(Trip.class))).thenReturn(1);

        tripUseCases.process(request, null, null);
        future.complete(1);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        var finalCaptor = tripCaptor;
        await().pollDelay(Duration.ofSeconds(1))
                .timeout(Duration.ofSeconds(30))
                .until(() -> {
                    verify(tripProvider).save(finalCaptor.capture());
                    return finalCaptor.getValue() != null;
                });
        verify(tripProvider).save(tripCaptor.capture());

        var requests = new LinkedList<>(request.requests());
        requests.sort(Comparator.comparing(RouteMessage.Request::getDesiredDate));
        var waypoints= new LinkedList<>(request.waypoints());

        var actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getEndTime().toLocalDateTime()).isEqualTo(requests.getLast().getDesiredDate());
        assertThat(actual.getStartTime().toLocalDateTime()).isEqualTo(requests.getFirst().getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.SENT_TO_CONTRACTOR);
        assertThat(actual.getWaypoints()).hasSameSizeAs(waypoints);
        Assertions.assertThat(actual.getCapacity()).isEqualTo(request.auto().capacity());
        assertThat(actual.getContractorId()).isEqualTo(request.contractorId());
        assertThat(actual.getLoaders()).isEqualTo(loaders);

        tripCaptor = ArgumentCaptor.forClass(Trip.class);

        verify(tripSender).send(tripCaptor.capture());

        actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getEndTime().toLocalDateTime()).isEqualTo(requests.getLast().getDesiredDate());
        assertThat(actual.getStartTime().toLocalDateTime()).isEqualTo(requests.getFirst().getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.SENT_TO_CONTRACTOR);
        assertThat(actual.getWaypoints()).hasSameSizeAs(request.waypoints());
        assertThat(actual.getCapacity()).isEqualTo(request.auto().capacity());
        assertThat(actual.getContractorId()).isEqualTo(request.contractorId());
        assertThat(actual.getRouteHumanReadableId()).isEqualTo(request.humanReadableId());

        RouteMessage.Request requestStartTime = request.requests().stream()
                .min(Comparator.comparing(RouteMessage.Request::getDesiredDate))
                .orElse(null);
        RouteMessage.Request requestEndTime = request.requests().stream()
                .max(Comparator.comparing(RouteMessage.Request::getDesiredDate))
                .orElse(null);

        assertThat(actual.getStartTime())
                .isEqualTo(ZonedDateTime.of(requestStartTime.getDesiredDate(), ZoneId.of(requestStartTime.getTimeZone())).toOffsetDateTime());

        assertThat(actual.getEndTime())
                .isEqualTo(ZonedDateTime.of(requestEndTime.getDesiredDate(), ZoneId.of(requestEndTime.getTimeZone())).toOffsetDateTime());
    }

    @Test
    @DisplayName("Проверка маршрутного листа. Проверка статуса")
    void test_process_cargo_status_2() {
        var request = Instancio.of(RouteMessage.class)
                .set(field(RouteMessage::status), "CARGO_DELIVERY_CONFIRMATION_FINISHED")
                .set(field(RouteMessage::timeZone), "+3")
                .create();

        request.requests()
                .forEach(r -> r.setTimeZone("+3"));

        var future = new CompletableFuture<Integer>();
        when(tripProvider.save(any(Trip.class))).thenReturn(1);

        tripUseCases.process(request, null, null);
        future.complete(1);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        var finalCaptor = tripCaptor;
        await().pollDelay(Duration.ofSeconds(1))
                .timeout(Duration.ofSeconds(30))
                .until(() -> {
                    verify(tripProvider).save(finalCaptor.capture());
                    return finalCaptor.getValue() != null;
                });
        verify(tripProvider).save(tripCaptor.capture());

        var requests = new LinkedList<>(request.requests());
        requests.sort(Comparator.comparing(RouteMessage.Request::getDesiredDate));
        var waypoints= new LinkedList<>(request.waypoints());

        var actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getEndTime().toLocalDateTime()).isEqualTo(requests.getLast().getDesiredDate());
        assertThat(actual.getStartTime().toLocalDateTime()).isEqualTo(requests.getFirst().getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.ORDER_FINISHED);
        assertThat(actual.getWaypoints()).hasSameSizeAs(waypoints);
        assertThat(actual.getCapacity()).isEqualTo(request.auto().capacity());
        assertThat(actual.getContractorId()).isEqualTo(request.contractorId());

        tripCaptor = ArgumentCaptor.forClass(Trip.class);

        verify(tripSender).send(tripCaptor.capture());

        actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getEndTime().toLocalDateTime()).isEqualTo(requests.getLast().getDesiredDate());
        assertThat(actual.getStartTime().toLocalDateTime()).isEqualTo(requests.getFirst().getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.ORDER_FINISHED);
        assertThat(actual.getWaypoints()).hasSameSizeAs(request.waypoints());
        assertThat(actual.getCapacity()).isEqualTo(request.auto().capacity());
        assertThat(actual.getContractorId()).isEqualTo(request.contractorId());
        assertThat(actual.getRouteHumanReadableId()).isEqualTo(request.humanReadableId());
    }

    @Test
    @DisplayName("Проверка маршрутного листа. Проверка статуса")
    void test_process_cargo_status_3() {
        var request = Instancio.of(RouteMessage.class)
                .set(field(RouteMessage::timeZone), "+3")
                .set(field(RouteMessage::status), "CARGO_CANCELED")
                .create();

        request.requests()
                .forEach(r -> r.setTimeZone("+3"));

        var future = new CompletableFuture<Integer>();
        when(tripProvider.save(any(Trip.class))).thenReturn(1);

        tripUseCases.process(request, null, null);
        future.complete(1);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        var finalCaptor = tripCaptor;
        await().pollDelay(Duration.ofSeconds(1))
                .timeout(Duration.ofSeconds(30))
                .until(() -> {
                    verify(tripProvider).save(finalCaptor.capture());
                    return finalCaptor.getValue() != null;
                });
        verify(tripProvider).save(tripCaptor.capture());

        var requests = new LinkedList<>(request.requests());
        requests.sort(Comparator.comparing(RouteMessage.Request::getDesiredDate));
        var waypoints= new LinkedList<>(request.waypoints());

        var actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getEndTime().toLocalDateTime()).isEqualTo(requests.getLast().getDesiredDate());
        assertThat(actual.getStartTime().toLocalDateTime()).isEqualTo(requests.getFirst().getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.ORDER_CANCELLED_BY_CLIENT);
        assertThat(actual.getWaypoints()).hasSameSizeAs(waypoints);
        assertThat(actual.getCapacity()).isEqualTo(request.auto().capacity());
        assertThat(actual.getContractorId()).isEqualTo(request.contractorId());

        tripCaptor = ArgumentCaptor.forClass(Trip.class);

        verify(tripSender).send(tripCaptor.capture());

        actual = tripCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getEndTime().toLocalDateTime()).isEqualTo(requests.getLast().getDesiredDate());
        assertThat(actual.getStartTime().toLocalDateTime()).isEqualTo(requests.getFirst().getDesiredDate());
        assertThat(actual.getStatus()).isEqualTo(TripStatus.ORDER_CANCELLED_BY_CLIENT);
        assertThat(actual.getWaypoints()).hasSameSizeAs(request.waypoints());
        assertThat(actual.getCapacity()).isEqualTo(request.auto().capacity());
        assertThat(actual.getContractorId()).isEqualTo(request.contractorId());
        assertThat(actual.getRouteHumanReadableId()).isEqualTo(request.humanReadableId());
    }

}