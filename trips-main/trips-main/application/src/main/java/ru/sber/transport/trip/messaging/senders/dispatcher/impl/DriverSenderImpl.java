package ru.sber.transport.trip.messaging.senders.dispatcher.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.trip.business.dto.DriverOnMapDto;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.database.trips.tables.records.ShiftRecord;
import ru.sber.transport.trip.messaging.ChannelType;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.messaging.providers.ShiftProvider;
import ru.sber.transport.trip.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trip.providers.driver.mapper.DriverMapper;
import ru.sber.transport.trip.providers.shift.mapper.ShiftMapper;
import ru.sber.transport.web_socket.handlers.WebSocketHandler;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
@NoAuthorize("/ws/**")
public class DriverSenderImpl extends WebSocketHandler<DriverOnMapDto> implements DriverSender {

    @Qualifier("driverOutput")
    private final ObjectProvider<OutputBridge> driverOutput;

    @Qualifier("driverOutputSsl")
    private final ObjectProvider<OutputBridge> driverOutputSsl;

    private final DriverMapper driverMapper;

    private final ShiftMapper shiftMapper;

    private final DispatcherProvider dispatcherProvider;

    private final ShiftProvider shiftProvider;

    @Override
    public void send(Driver driver) {
        send(driver, ChannelType.BOTH);
    }

    @Override
    public void send(Driver driver, ChannelType channel) {
        if (ChannelType.BOTH.equals(channel) || ChannelType.KAFKA.equals(channel)) {
            sendToKafka(driver);
        }
        if (ChannelType.BOTH.equals(channel) || ChannelType.WEB_SOCKET.equals(channel)) {
            sendToWebsoket(driver);
        }
    }

    private void sendToKafka(Driver driver) {
        var message = driverMapper.toMessage(driver);
        driverOutput.ifAvailable(ob -> ob.send(message, Map.of("source", Source.TRIPS.name())));
        driverOutputSsl.ifAvailable(ob -> ob.send(message, Map.of("source", Source.TRIPS.name())));
    }

    private void sendToWebsoket(Driver driver) {
        var authorizedSessionsIds = getAuthorizedSessionsIds();
        if (authorizedSessionsIds.isEmpty()) {
            log.debug("Authorized sessions is empty, sending cancelled");
        } else {
            try {
                var dispatchers = dispatcherProvider.findAllByContractorIdAndIdIn(driver.getContractorId(), authorizedSessionsIds);
                var message = driverMapper.toDriverOnMapDto(driver);

                if (driver.isOnline()) {
                    var shift = shiftProvider.get(driver.getShiftId())
                            .orElseThrow(() -> new EntityNotFoundException(ShiftRecord.class, driver.getShiftId()));

                    var shiftDto = shiftMapper.toOnMapShiftDto(shift);
                    message.setCurrentShift(shiftDto);
                }

                log.debug("Sending to {} dispatchers", dispatchers.size());
                for (var dispatcher : dispatchers) {
                    send(dispatcher.getId(), message);
                }
                log.debug("Message sent with WebSocket");
            } catch (Exception e) {
                log.error("sendToWebsoket: Error!", e);
            }
        }
    }

    @Override
    public String url() {
        return "/driverPositions/v2/*";
    }
}
