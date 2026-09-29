package ru.sber.transport.trip.messaging.senders.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.message.TripMessage;
import ru.sber.transport.trip.messaging.mapper.*;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.messaging.providers.ShiftProvider;
import ru.sber.transport.trip.messaging.senders.TripSender;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Проверка отправителя")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
class TripSenderImplTest {

    private final OutputBridge outputBridge = mock(OutputBridge.class);

    private final OutputBridge outputSslBridge = mock(OutputBridge.class);

    private final ContractorProvider contractorProvider = mock(ContractorProvider.class);

    private final ShiftProvider shiftProvider = mock(ShiftProvider.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WaypointMapper waypointMapper = new WaypointMapperImpl() {
        @Override
        public ObjectMapper objectMapper() {
            return objectMapper;
        }
    };

    private final RequestMapper requestMapper = new RequestMapperImpl(waypointMapper);

    private final MessageTripMapper messageTripMapper = new MessageTripMapperImpl(requestMapper, waypointMapper) {
        @Override
        public ObjectMapper objectMapper() {
            return objectMapper;
        }
    };

    private final TripSender tripSender = new TripSenderImpl(new ObjectProvider<>() {
        @Override
        public OutputBridge getObject(Object... args) throws BeansException {
            return outputBridge;
        }

        @Override
        public OutputBridge getIfAvailable() throws BeansException {
            return outputBridge;
        }

        @Override
        public OutputBridge getIfUnique() throws BeansException {
            return outputBridge;
        }

        @Override
        public OutputBridge getObject() throws BeansException {
            return outputBridge;
        }
    }, new ObjectProvider<>() {
        @Override
        public OutputBridge getObject(Object... args) throws BeansException {
            return outputSslBridge;
        }

        @Override
        public OutputBridge getIfAvailable() throws BeansException {
            return outputSslBridge;
        }

        @Override
        public OutputBridge getIfUnique() throws BeansException {
            return outputSslBridge;
        }

        @Override
        public OutputBridge getObject() throws BeansException {
            return outputSslBridge;
        }
    }, messageTripMapper, contractorProvider, shiftProvider);

    @Test
    @DisplayName("Отправка")
    void test_send() {
        objectMapper.registerModule(new JavaTimeModule());

        var trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setContractorId(UUID.randomUUID());
        trip.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));
        trip.setStatus(TripStatus.TRIP_IN_PROGRESS);
        trip.setTaxiClass(TaxiClass.ECONOMY.name());
        trip.setPassengerCount(2);
        trip.setDigitId(1L);
        trip.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 0.0, 1.1, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                null,
                null, null)));
        trip.setRequests(Set.of(createRequest(UUID.randomUUID())));

        tripSender.send(trip);

        var captor = ArgumentCaptor.forClass(TripMessage.class);

        verify(outputBridge).send(captor.capture());

        var actual = captor.getValue();
        assertThat(actual.getId()).isEqualTo(trip.getId());
        assertThat(actual.getContractorId()).isEqualTo(trip.getContractorId());
        assertThat(actual.getExpectedStartTime()).isEqualTo(trip.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
        assertThat(actual.getExpectedEndTime()).isEqualTo(trip.getExpectedEndTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
        assertThat(actual.getStatus()).isEqualTo(trip.getStatus().name());
        assertThat(actual.getAdditional()).isInstanceOf(TripMessage.AdditionalData.class);
        assertThat(actual.getAdditional().taxiClass()).isEqualTo(trip.getTaxiClass());
        assertThat(actual.getAdditional().passengerCount()).isEqualTo(trip.getPassengerCount());
        assertThat(actual.getWaypoints()).hasSameSizeAs(trip.getWaypoints());
        assertThat(actual.getRequests()).hasSameSizeAs(trip.getRequests());

        trip.setTaxiClass(null);
        tripSender.send(trip);

        captor = ArgumentCaptor.forClass(TripMessage.class);

        verify(outputBridge, times(2)).send(captor.capture());

        actual = captor.getValue();
        assertThat(actual.getId()).isEqualTo(trip.getId());
        assertThat(actual.getContractorId()).isEqualTo(trip.getContractorId());
        assertThat(actual.getExpectedStartTime()).isEqualTo(trip.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
        assertThat(actual.getExpectedEndTime()).isEqualTo(trip.getExpectedEndTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
        assertThat(actual.getStatus()).isEqualTo(trip.getStatus().name());
        assertThat(actual.getAdditional()).isInstanceOf(TripMessage.AdditionalData.class);
        assertThat(actual.getAdditional().taxiClass()).isNull();
        assertThat(actual.getAdditional().passengerCount()).isEqualTo(trip.getPassengerCount());
        assertThat(actual.getWaypoints()).hasSameSizeAs(trip.getWaypoints());
        assertThat(actual.getRequests()).hasSameSizeAs(trip.getRequests());
    }

    private Request createRequest(UUID id) {
        var request = new Request();

        request.setId(id);
        request.setAuthorId(UUID.randomUUID());
        request.setPassengerId(UUID.randomUUID());
        request.setTaxiClass(TaxiClass.ECONOMY.name());
        request.setPassengerCount(1);
        request.setExpected(new ExpectedData(0, 1.1, Duration.ofSeconds(2)));
        request.setCreationTime(OffsetDateTime.now(ZoneOffset.UTC));
        request.setDesiredDate(OffsetDateTime.now(ZoneOffset.UTC));
        request.setCoopTrip(false);
        request.setSuburb(true);
        request.setTimeZone("TZ");
        request.setTariffId(UUID.randomUUID());
        request.setContractorId(UUID.randomUUID());
        request.setStatus(TripRequestStatus.TAXI_WAYPOINT_ARRIVED.name());
        request.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 1.1, 2.2, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                null,
                null, null)));

        return request;
    }

}