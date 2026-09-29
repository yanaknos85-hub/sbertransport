package ru.sber.transport.driver_track.messaging.sender.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.messaging.TripFactDistanceMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка отправителя фактической дистанции")
public class FactDistanceSenderImplTest {

    @Test
    @DisplayName("Отправка")
    void sendTest() {
        var outputBridge = mock(OutputBridge.class);
        var objectProvider = new ObjectProvider<OutputBridge>() {

            @Override
            public OutputBridge getObject() throws BeansException {
                return outputBridge;
            }

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
        };
        var sender = new FactDistanceSenderImpl(objectProvider, objectProvider);
        var distance = Instancio.create(Double.class);
        var uuid = Instancio.create(UUID.class);

        sender.send(Map.of(RouteSource.FORMULA, new RouteDTO(distance, null, null)), uuid);

        var messageCaptor = ArgumentCaptor.forClass(TripFactDistanceMessage.class);
        verify(outputBridge, times(2)).send(messageCaptor.capture());

        var sentMessages = messageCaptor.getAllValues();
        for (var sentMessage : sentMessages) {
            assertEquals(distance, sentMessage.getDistances().get("FORMULA"));
            assertEquals(uuid, sentMessage.getId());
        }
    }
}
