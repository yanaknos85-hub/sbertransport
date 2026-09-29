package ru.sberbank.ditsib.transport.messaging.listeners;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.integrations.messaging.OrdersLocationMessage;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.FraudRepository;
import ru.sberbank.ditsib.transport.request.database.model.FraudData;
import ru.sberbank.ditsib.transport.request.database.model.FraudType;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.TaxiTrip;
import ru.sberbank.ditsib.transport.request.messaging.message.FraudMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.RequestFactDataMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.ResourceIdMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.TaxiTripMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.TaxiTripSender;
import ru.sberbank.ditsib.transport.request.provider.CarLocationProvider;
import ru.sberbank.ditsib.transport.request.service.TripService;
import ru.sberbank.ditsib.transport.request.service.impl.RequestForTaxiServiceImpl;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static ru.sberbank.ditsib.transport.request.database.model.FraudType.RADIUS;
import static ru.sberbank.ditsib.transport.request.database.model.FraudType.RECEIPT;

@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class, properties = "extlogging.kafka: false")
@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка слушателей кафки")
class ListenerConfigTest extends KafkaTest {

    @MockitoBean
    private CarLocationProvider carLocationProvider;

    @MockitoBean
    private FraudRepository fraudRepository;

    @MockitoBean
    private TripService tripService;

    @MockitoBean
    private TaxiTripSender taxiTripSender;

    @MockitoBean
    private RequestForTaxiServiceImpl requestForTaxiService;

    @Autowired
    @Qualifier("fraudInput")
    private Consumer<Message<FraudMessage>> fraudInput;

    @Autowired
    @Qualifier("fraudInputRadius")
    private Consumer<Message<FraudMessage>> fraudInputRadius;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;
    @Captor
    private ArgumentCaptor<OrdersLocationMessage> orderLocationMessageArgumentCaptor;

    @Test
    @DisplayName("Проверка входящий сообщения для заполнения фактических данных о поездке")
    void enrichRequestViaFactData() {
        doReturn(Optional.of(Instancio.of(RequestForTaxi.class)
                .set(Select.field(RequestForTaxi::getTaxiTrip), Instancio.of(TaxiTrip.class)
                        .set(Select.field(TaxiTrip::getDriver), null)
                        .create())
                .create()))
                .when(requestForTaxiService).findByHumanReadableId("OT-0001-00024093");

        var message = new RequestFactDataMessage(
                UUID.randomUUID(),
                "OT-0001-00024093",
                3333.0,
                15.0,
                5.0,
                "RG-0001-00000215",
                false
        );

        var requestFactDataMessageArgumentCaptor = ArgumentCaptor.forClass(RequestFactDataMessage.class);
        doNothing().when(tripService).updateFactData(any(RequestFactDataMessage.class));

        var taxiTripMessageArgumentCaptor = ArgumentCaptor.forClass(TaxiTripMessage.class);
        doNothing().when(taxiTripSender).send(any(TaxiTripMessage.class));

        produceMessage("service.trip.registry.check.contractorRequest", message);

        verify(tripService).updateFactData(requestFactDataMessageArgumentCaptor.capture());
        verify(taxiTripSender).send(taxiTripMessageArgumentCaptor.capture());

        var capturedUpdateFactDataMessage = requestFactDataMessageArgumentCaptor.getValue();
        var capturedTaxiTripMessage = taxiTripMessageArgumentCaptor.getValue();

        assertThat(capturedUpdateFactDataMessage)
                .extracting(
                        RequestFactDataMessage::getFactTotalWaitingTime,
                        RequestFactDataMessage::getRegistryHrId,
                        RequestFactDataMessage::getFactCost,
                        RequestFactDataMessage::getFactDistance,
                        RequestFactDataMessage::getIsPaid,
                        RequestFactDataMessage::getHrId
                )
                .containsExactly(
                        message.getFactTotalWaitingTime(),
                        message.getRegistryHrId(),
                        message.getFactCost(),
                        message.getFactDistance(),
                        message.getIsPaid(),
                        message.getHrId()
                );

        assertThat(capturedTaxiTripMessage)
                .extracting(
                        TaxiTripMessage::getRegistryFactWaitingTime,
                        TaxiTripMessage::getRegistryHumanReadableId,
                        TaxiTripMessage::getRegistryFactCost,
                        TaxiTripMessage::getRegistryFactDistance,
                        TaxiTripMessage::getRegistryFactPayment
                )
                .containsExactly(
                        message.getFactTotalWaitingTime(),
                        message.getRegistryHrId(),
                        message.getFactCost(),
                        message.getFactDistance(),
                        message.getIsPaid()
                );
    }

    @Test
    @DisplayName("Проверка входящих сообщений с местоположением по заявке")
    void orderLocationResponseInput() {
        doNothing().when(carLocationProvider).sendLocationsToSubscribers(orderLocationMessageArgumentCaptor.capture());

        var ordersLocation = List.of(
                new OrdersLocationMessage.OrderLocationMessage(
                        "testOrderPartnerId1",
                        new OrdersLocationMessage.OrderCoordinatesMessage(
                                100.0,
                                200.0
                        ),
                        120
                ),
                new OrdersLocationMessage.OrderLocationMessage(
                        "testOrderPartnerId2",
                        new OrdersLocationMessage.OrderCoordinatesMessage(
                                300.0,
                                400.0
                        ),
                        240
                )
        );
        var message = new OrdersLocationMessage(UUID.randomUUID(), ordersLocation);

        produceMessage("service.response.car-location-response", message);

        var capturedMessage = orderLocationMessageArgumentCaptor.getValue();
        assertThat(capturedMessage.orderLocations())
                .usingRecursiveComparison()
                .isEqualTo(ordersLocation);
    }

    @Test
    @DisplayName("Проверка входящих сообщений с местоположением транспорта")
    void carLocationResourceIdInput() {
        doNothing().when(carLocationProvider).deactivateTask(uuidArgumentCaptor.capture());

        var id = UUID.randomUUID();
        var message = new ResourceIdMessage(id);

        produceMessage("car_location_resource_id", message);

        var capturedId = uuidArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(id);
    }

    public static Stream<Arguments> fraudData() {
        return Stream.of(
                Arguments.of(RECEIPT, new FraudMessage(UUID.randomUUID(), Instancio.create(String.class)), (Function<Object, UUID>) msg -> ReflectionUtils.<FraudMessage>cast(msg).getId(), (Function<Object, String>) msg -> ReflectionUtils.<FraudMessage>cast(msg).comment()),
                Arguments.of(RADIUS, new FraudMessage(UUID.randomUUID(), Instancio.create(String.class)), (Function<Object, UUID>) msg -> ReflectionUtils.<FraudMessage>cast(msg).getId(), (Function<Object, String>) msg -> ReflectionUtils.<FraudMessage>cast(msg).comment())
        );
    }

    @ParameterizedTest
    @MethodSource("fraudData")
    @DisplayName("Получение информации о фроде")
    void test_fraud(FraudType fraudType, Object message, Function<Object, UUID> idFunc, Function<Object, String> commentFunc) {
        final var expectedId = idFunc.apply(message);
        final var expectedComment = commentFunc.apply(message);

        when(fraudRepository.existsByRequestIdAndType(any(UUID.class), any(FraudType.class))).thenReturn(false);

        when(fraudRepository.saveAndFlush(any(FraudData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final var rawMessage = MessageBuilder
                .createMessage(ReflectionUtils.cast(message),
                        new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, expectedId)));

        switch (fraudType) {
            case RECEIPT -> fraudInput.accept(ReflectionUtils.cast(rawMessage));
            case RADIUS -> fraudInputRadius.accept(ReflectionUtils.cast(rawMessage));
        }

        ArgumentCaptor<FraudData> fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudRepository).saveAndFlush(fraudDataCaptor.capture());

        final var actual = fraudDataCaptor.getValue();

        assertThat(actual).isNotNull();
        assertThat(actual.getRequest().getId()).isEqualTo(expectedId);
        assertThat(actual.getType()).isEqualTo(fraudType);
        assertThat(actual.getComment()).isEqualTo(expectedComment);
    }
}
