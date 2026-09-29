package ru.sber.transport.dispatcher.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import ru.sber.transport.authorization.annotations.SkipConsentCheck;
import ru.sber.transport.dispatcher.database.dao.DispatcherRepository;
import ru.sber.transport.dispatcher.database.model.Shift;
import ru.sber.transport.dispatcher.dto.ShiftEwbSocketDto;
import ru.sber.transport.dispatcher.dto.SignEwbRequestDto;
import ru.sber.transport.dispatcher.mappers.ShiftMapper;
import ru.sber.transport.dispatcher.messages.EwbMessage;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.dispatcher.messaging.senders.ShiftSender;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.web_socket.handlers.WebSocketHandler;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
@SkipConsentCheck("/ws/")
@NoAuthorize("/ws/**")
public class ShiftSenderImpl extends WebSocketHandler<ShiftEwbSocketDto> implements ShiftSender {

    @Qualifier("shiftOutput")
    private final ObjectProvider<OutputBridge> shiftOutput;

    @Qualifier("shiftOutputSsl")
    private final ObjectProvider<OutputBridge> shiftOutputSsl;

    @Qualifier("ewbOutput")
    private final ObjectProvider<OutputBridge> ewbOutput;

    @Qualifier("ewbOutputSsl")
    private final ObjectProvider<OutputBridge> ewbOutputSsl;

    private final ShiftMapper shiftMapper;

    private final DispatcherRepository dispatchRepository;

    @Override
    public void send(Shift shift, Source source) {
        var message = shiftMapper.toMessage(shift);

        shiftOutput.ifAvailable(ob -> ob.send(message, Map.of("source", source.name())));
        shiftOutputSsl.ifAvailable(ob -> ob.send(message, Map.of("source", source.name())));
    }

    @Override
    public void sendForEwb(List<SignEwbRequestDto> signRequests) {
        signRequests.stream().map(shiftMapper::toEwbMessage).forEach(message -> {
            ewbOutput.ifAvailable(outputBridge -> outputBridge.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
            ewbOutputSsl.ifAvailable(outputBridge -> outputBridge.send(message, Map.of(KafkaHeaders.KEY, message.getId())));
        });

    }

    @Override
    public void sendBySocket(Shift shift, String errorMessage, boolean success) {
        var authorizedSessionsIds = getAuthorizedSessionsIds();
        if(authorizedSessionsIds.isEmpty()) {
            log.debug("Authorized sessions is empty, sending cancelled");
            return;
        }
        var dispatchers = dispatchRepository.findAllByContractorIdAndIdIn(shift.getContractorId(), authorizedSessionsIds);
        dispatchers.forEach(dispatcher -> send(dispatcher.getId(), new ShiftEwbSocketDto(shift.getId(), success, errorMessage)));
    }

    @Override
    public String url() {
        return "/shifts/ewb/*";
    }
}
