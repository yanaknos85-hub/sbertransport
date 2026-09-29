package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.Message;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingTripRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTrip;
import ru.sberbank.ditsib.transport.request.mappers.RequestMapper;

/**
 * Реализация отправителя.
 */
@Component
@Slf4j
@Transactional
public class CarsharingRequestSenderImpl extends BaseRequestSender<RequestForCarsharing> {
    
    private final RequestMapper mapper;
    
    private final CarsharingTripRepository carsharingTripRepository;

    public CarsharingRequestSenderImpl(ObjectProvider<OutputBridge> requestOutput, RequestMapper mapper, CarsharingTripRepository carsharingTripRepository) {
        super(requestOutput);
        this.mapper = mapper;
        this.carsharingTripRepository = carsharingTripRepository;
    }

    @Override
    protected Message<?> toMessage(RequestForCarsharing request, boolean deleted) {
        var rentId = carsharingTripRepository.findFirstByRequestId(request.getId()).map(CarsharingTrip::getRentId).orElse(null);
        return mapper.toMessage(request, rentId, deleted);
    }

    @Override
    public TransportTypeEnum type() {
        return TransportTypeEnum.CARSHARING;
    }
}
