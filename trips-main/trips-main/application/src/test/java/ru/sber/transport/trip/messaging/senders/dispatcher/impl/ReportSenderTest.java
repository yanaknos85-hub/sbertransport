package ru.sber.transport.trip.messaging.senders.dispatcher.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.support.KafkaHeaders;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.trip.messaging.senders.ReportSender;
import ru.sber.transport.trip.messaging.senders.impl.ReportSenderImpl;
import ru.sber.transport.trip_reports.message.TripReportMessage;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@DisplayName("Проверка отправителя отчетов по поездке")
class ReportSenderTest {

    private final OutputBridge reportOutput = mock(OutputBridge.class);

    private final ObjectProvider<OutputBridge> objectProvider = new ObjectProvider<>() {
        @Override
        public OutputBridge getObject(Object... args) throws BeansException {
            return reportOutput;
        }

        @Override
        public OutputBridge getIfAvailable() throws BeansException {
            return reportOutput;
        }

        @Override
        public OutputBridge getIfUnique() throws BeansException {
            return reportOutput;
        }

        @Override
        public OutputBridge getObject() throws BeansException {
            return reportOutput;
        }
    };
    private final ReportSender reportSender = new ReportSenderImpl(objectProvider, objectProvider);

    @Test
    @DisplayName("Проверка отправки по кафке")
    void test_kafka() {
        var reportMessage = Instancio.create(TripReportMessage.class);
        reportSender.send(reportMessage);
        assertKafka(reportMessage);
    }

    private void assertKafka(TripReportMessage reportMessage) {
        var payloadCaptor = ArgumentCaptor.forClass(TripReportMessage.class);
        var headersCaptor = ArgumentCaptor.forClass(Map.class);

        verify(reportOutput, times(2)).send(payloadCaptor.capture(), headersCaptor.capture());

        var value = payloadCaptor.getAllValues().get(0);
        assertEquals(reportMessage.getContractorId(), value.getContractorId());
        assertEquals(reportMessage.getId(), value.getId());

        var headers = headersCaptor.getAllValues().get(0);
        assertEquals(reportMessage.getId(), headers.get(KafkaHeaders.KEY));
    }
}
