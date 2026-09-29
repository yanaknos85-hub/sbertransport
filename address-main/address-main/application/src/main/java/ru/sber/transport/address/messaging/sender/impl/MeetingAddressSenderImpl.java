package ru.sber.transport.address.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.address.messaging.mapper.MeetingAddressMapper;
import ru.sber.transport.address.messaging.sender.AddressSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

@Slf4j
@RequiredArgsConstructor
@Component
class MeetingAddressSenderImpl implements AddressSender<MeetingAddress> {

    @Qualifier("addressMessageOutputAvro")
    private final ObjectProvider<OutputBridge> addressMessageOutputAvro;
    
    private final MeetingAddressMapper mapper;
    
    @Override
    public void send(MeetingAddress meetingAddress, boolean deleted) {
        if (!deleted) {
            log.debug("Send meeting address: {}", meetingAddress);
            addressMessageOutputAvro.ifAvailable(ob -> ob.send(mapper.toMessage(meetingAddress)));
        }
    }

}
