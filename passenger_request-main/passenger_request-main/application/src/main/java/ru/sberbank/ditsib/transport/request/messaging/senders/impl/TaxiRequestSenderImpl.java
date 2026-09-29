package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.Message;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.mappers.RequestMapper;

/**
 * Реализация отправителя.
 */
@Component
@Slf4j
@Transactional
public class TaxiRequestSenderImpl extends BaseRequestSender<RequestForTaxi> {

    private final RequestMapper mapper;

    public TaxiRequestSenderImpl(ObjectProvider<OutputBridge> requestOutput, RequestMapper mapper) {
        super(requestOutput);
        this.mapper = mapper;
    }

    @Override
    protected Message<?> toMessage(RequestForTaxi request, boolean deleted) {
        return mapper.toMessage(request, deleted);
    }

    @Override
    public TransportTypeEnum type() {
        return TransportTypeEnum.TAXI;
    }
}
