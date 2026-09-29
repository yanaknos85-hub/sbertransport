package ru.sberbank.ditsib.transport.request.provider.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.request.mappers.TaxiTripMapper;
import ru.sberbank.ditsib.transport.request.messaging.message.RequestFactDataMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.TaxiTripSender;
import ru.sberbank.ditsib.transport.request.provider.RequestFactDataProvider;
import ru.sberbank.ditsib.transport.request.service.TripService;
import ru.sberbank.ditsib.transport.request.service.impl.RequestForTaxiServiceImpl;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RequestFactDataProviderImpl implements RequestFactDataProvider {

    private final TripService tripService;
    private final RequestForTaxiServiceImpl requestForTaxiService;
    private final TaxiTripMapper mapper;
    private final TaxiTripSender taxiTripSender;

    @Override
    public void enrichRequest(RequestFactDataMessage message) {
        requestForTaxiService.findByHumanReadableId(message.getHrId())
                .flatMap(request -> Optional.ofNullable(request.getTaxiTrip()))
                .ifPresent(trip -> {
                    mapper.toTaxiTrip(trip, message);
                    tripService.updateFactData(message);
                    taxiTripSender.send(mapper.toMessage(trip));
                });
    }
}
