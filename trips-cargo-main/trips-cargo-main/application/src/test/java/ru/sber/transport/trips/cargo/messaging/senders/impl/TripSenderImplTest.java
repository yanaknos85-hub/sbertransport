package ru.sber.transport.trips.cargo.messaging.senders.impl;

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
import ru.sber.transport.trips.cargo.business.model.*;
import ru.sber.transport.trips.cargo.message.TripMessage;
import ru.sber.transport.trips.cargo.messaging.mapper.MessageTripMapper;
import ru.sber.transport.trips.cargo.messaging.mapper.MessageTripMapperImpl;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.messaging.senders.TripSender;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Проверка отправителя")
@UnitTest
@IsolatedTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Feature("app_platform_trips_cargo")
class TripSenderImplTest {

    private final OutputBridge outputBridge = mock(OutputBridge.class);

    private final OutputBridge outputSslBridge = mock(OutputBridge.class);

    private final ContractorProvider contractorProvider = mock(ContractorProvider.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MessageTripMapper messageTripMapper = new MessageTripMapperImpl() {
        @Override
        public ObjectMapper objectMapper() {
            return objectMapper;
        }
    };

    private final ObjectProvider<OutputBridge> bridge = new ObjectProvider<>() {
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
    };

    private final ObjectProvider<OutputBridge> bridgeSsl = new ObjectProvider<>() {
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
    };

    private final TripSender tripSender = new TripSenderImpl(bridge, bridgeSsl, messageTripMapper, contractorProvider);

    @Test
    @DisplayName("Отправка")
    void test_send() {
        objectMapper.registerModule(new JavaTimeModule());

        var trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setContractorId(UUID.randomUUID());
        trip.setStartTime(OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC));
        trip.setEndTime(OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC).plusDays(1));
        trip.setStatus(TripStatus.SENT_TO_CONTRACTOR);
        trip.setCapacity(2.0);
        trip.setDigitId(1L);
        trip.setWaypoints(List.of(new Waypoint(UUID.randomUUID(), 0.0, 1.1, 0,
                "country",
                "region",
                "city",
                "street",
                "house",
                "building",
                Duration.ZERO,
                "address",
                null)));
        trip.setRequests(Set.of(createRequest(UUID.randomUUID())));

        tripSender.send(trip);

        var captor = ArgumentCaptor.forClass(TripMessage.class);

        verify(outputBridge).send(captor.capture());

        var actual = captor.getValue();
        assertThat(actual.getId()).isEqualTo(trip.getId());
        assertThat(actual.getContractorId()).isEqualTo(trip.getContractorId());
        assertThat(actual.getStartTime()).isEqualTo(trip.getStartTime().toLocalDateTime());
        assertThat(actual.getEndTime()).isEqualTo(trip.getEndTime().toLocalDateTime());
        assertThat(actual.getStatus()).isEqualTo(trip.getStatus().name());
        assertThat(actual.getAdditional()).isInstanceOf(TripMessage.AdditionalData.class);
        assertThat(actual.getAdditional().capacity()).isEqualTo(trip.getCapacity());
        assertThat(actual.getWaypoints()).hasSameSizeAs(trip.getWaypoints());
        assertThat(actual.getRequests()).hasSameSizeAs(trip.getRequests());
    }

    private CargoRequest createRequest(UUID id) {
        var request = new CargoRequest();
        var author = new Employee();
        author.setId(UUID.randomUUID());
        var recipient = new Employee();
        recipient.setId(UUID.randomUUID());
        request.setId(id);
        request.setAuthor(author);
        request.setSender(author);
        request.setRecipient(recipient);
        request.setCreationTime(OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC));
        request.setDesiredDate(OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC));
        request.setTimeZone("TZ");
        request.setTariffId(UUID.randomUUID());
        request.setStatus("CARGO_AWAITING_DATA");
        return request;
    }

}