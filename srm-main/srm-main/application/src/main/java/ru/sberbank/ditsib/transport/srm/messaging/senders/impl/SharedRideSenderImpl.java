package ru.sberbank.ditsib.transport.srm.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.srm.model.SrmRequestKpiDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.trip.SharedRideMessage;
import ru.sberbank.ditsib.transport.srm.messaging.senders.SharedRideSender;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class SharedRideSenderImpl implements SharedRideSender {
    
    @Qualifier("sharedRideOutput")
    private final ObjectProvider<OutputBridge> sharedRideOutput;
    
    @Override
    public void send(SrmSharedRideDTO sharedRide) {
        sharedRideOutput.ifAvailable(it -> {
            final var message = toMessage(sharedRide);
            it.send(message, Map.of(KafkaHeaders.KEY, message.getId()));
        });
    }
    
    private SharedRideMessage toMessage(SrmSharedRideDTO sharedRideDTO) {
        UUID kpiId = UUID.randomUUID();
        
        final var orderKpiList = new ArrayList<SharedRideMessage.OrderKpi>();
        for (final var requestKpi : sharedRideDTO.getRequestKpiList()) {
            final var orderKpi = new SharedRideMessage.OrderKpi();
            orderKpi.setId(requestKpi.getId());
            orderKpi.setKpi_id(kpiId);
            orderKpi.setOrderId(requestKpi.getId());
            orderKpi.setOrderDistanceKm(requestKpi.getRequestDistance().intValue());
            orderKpi.setRideTimeMin(requestKpi.getRequestTime().intValue());
            orderKpi.setCostSharePart(requestKpi.getCostSharePart());
            orderKpi.setSavings(requestKpi.getSavingsCash().doubleValue());
            orderKpi.setSavingsPct(requestKpi.getSavingsProcents());
            
            orderKpiList.add(orderKpi);
        }
        
        SharedRideMessage.SharedRideKPI kpi = new SharedRideMessage.SharedRideKPI();
        kpi.setId(kpiId);
        kpi.setTotalCost(sharedRideDTO.getRideCost().doubleValue());
        kpi.setTotalTimeMin(sharedRideDTO.getRideTime().intValue());
        kpi.setTotalDistanceKm(sharedRideDTO.getRideDistance());
        kpi.setOrdersKpi(orderKpiList);
        
        SharedRideMessage message = new SharedRideMessage();
        message.setId(sharedRideDTO.getId());
        if ((sharedRideDTO.getTransportType() == TransportTypeEnum.TAXI)
            || (sharedRideDTO.getTransportType() == TransportTypeEnum.PERSONAL)) {
            message.setPassengers(sharedRideDTO.getRequestKpiList().stream().mapToInt(SrmRequestKpiDTO::getRequiredPassengers).sum());
        }
        message.setTariffId(sharedRideDTO.getTariffId());
        message.setActive(sharedRideDTO.isActive());
        message.setKpi(kpi);
        
        return message;
    }
}
