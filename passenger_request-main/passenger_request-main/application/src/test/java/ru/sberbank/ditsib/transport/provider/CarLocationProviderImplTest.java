package ru.sberbank.ditsib.transport.provider;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.integrations.messaging.OrdersLocationMessage;
import ru.sberbank.ditsib.transport.LoggingExtension;
import ru.sberbank.ditsib.transport.request.dto.OrderLocationDto;
import ru.sberbank.ditsib.transport.request.mappers.CarLocationMapper;
import ru.sberbank.ditsib.transport.request.provider.impl.CarLocationProviderImpl;
import ru.sberbank.ditsib.transport.request.service.CarLocationService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarLocationProviderImplTest {

    @Mock
    private CarLocationMapper carLocationMapper;
    @Mock
    private CarLocationService carLocationService;
    @InjectMocks
    private CarLocationProviderImpl carLocationProvider;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(CarLocationProviderImpl.class);

    @Test
    void sendLocationsToSubscribers() {
        var messageId = UUID.randomUUID();
        var message = new OrdersLocationMessage(
                messageId,
                List.of(
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
                )
        );
        doReturn(
                List.of(
                        new OrderLocationDto(
                                "testOrderPartnerId1",
                                new OrderLocationDto.OrderCoordinates(
                                        100.0,
                                        200.0
                                ),
                                120
                        ),
                        new OrderLocationDto(
                                "testOrderPartnerId2",
                                new OrderLocationDto.OrderCoordinates(
                                        300.0,
                                        400.0
                                ),
                                240
                        )
                )
        ).when(carLocationMapper).ordersLocationMessageListToOrdersLocationDtoList(message.orderLocations());
        carLocationProvider.sendLocationsToSubscribers(message);
        verify(carLocationService, times(2)).sendLocationToSubscriber(any(OrderLocationDto.class));
    }

    static Stream<Arguments> sendLocationsToSubscribers_null_payload() {
        return Stream.of(
                Arguments.of(
                        null,
                        "Payload is null"
                ),
                Arguments.of(
                        Instancio.of(OrdersLocationMessage.class)
                                .ignore(Select.field(OrdersLocationMessage::orderLocations))
                                .create(),
                        "Payload with id {} has orderLocations is null"
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void sendLocationsToSubscribers_null_payload(OrdersLocationMessage message, String expectedLogMessage) {
        carLocationProvider.sendLocationsToSubscribers(message);

        assertThat(LOGGING_EXTENSION.getEvents()).hasSize(1);
        var logEvent = LOGGING_EXTENSION.getEvents().getLast();
        assertThat(logEvent)
                .extracting(
                        ILoggingEvent::getLevel,
                        ILoggingEvent::getMessage
                )
                .containsExactly(
                        Level.INFO,
                        expectedLogMessage
                );
    }

    @Test
    void deactivateTask() {
        var requestId = UUID.randomUUID();
        carLocationProvider.deactivateTask(requestId);
        verify(carLocationService).deactivateTask(requestId);
    }
}
