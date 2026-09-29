package ru.sber.transport.trip.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.trip.messaging.senders.ReportSender;
import ru.sber.transport.trip_reports.message.TripReportMessage;

import java.util.Map;

@Component
@Transactional
@RequiredArgsConstructor
public class ReportSenderImpl implements ReportSender {

    @Qualifier("reportOutput")
    private final ObjectProvider<OutputBridge> reportOutput;

    @Qualifier("reportOutputSsl")
    private final ObjectProvider<OutputBridge> reportOutputSsl;

    @Override
    public void send(TripReportMessage reportMessage) {
        Map<String, Object> headers = Map.of(KafkaHeaders.KEY, reportMessage.getId());
        reportOutput.ifAvailable(ob -> ob.send(reportMessage, headers));
        reportOutputSsl.ifAvailable(ob -> ob.send(reportMessage, headers));
    }
}
