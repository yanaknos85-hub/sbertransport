package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.messaging.messages.trip.TaxiTripMessage;
import ru.sberbank.ditsib.transport.reports.dao.SharedRideRepository;
import ru.sberbank.ditsib.transport.reports.mappers.TaxiTripMapper;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.TaxiTripListener;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.reports.service.RequestService;
import ru.sberbank.ditsib.transport.reports.service.TariffService;
import ru.sberbank.ditsib.transport.reports.service.TaxiTripService;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component("taxiTripInput")
@Slf4j
public class TaxiTripListenerImpl implements TaxiTripListener {
    
    private final TaxiTripService taxiTripService;
    private final TaxiTripMapper mapper;
    private final RequestService requestService;
    private final TariffService<TaxiTariff> tariffService;
    private final SharedRideRepository sharedRideRepository;
    
    @Override
    public void handle(TaxiTripMessage message, UUID headerId) {
        if (!message.isDeleted()) {
            var id = Optional.ofNullable(message.getId()).orElse(headerId);
            TripType tripType = TripType.valueOf(message.getTripType());
            if (tripType == TripType.SINGLE) {
                SingleTaxiTrip singleTaxiTrip = taxiTripService.findSingleTripById(id).orElse(null);
                
                if (singleTaxiTrip == null) {
                    TaxiTariff taxiTariff = tariffService.findById(message.getTariffId())
                                                         .orElseGet(() ->
                                                                            tariffService.save(
                                                                                    TaxiTariff.builder()
                                                                                              .id(message.getTariffId())
                                                                                              .transportType(TransportTypeEnum.TAXI)
                                                                                              .build()));
                    Request request = requestService.findById(message.getRequestId())
                                                    .orElseGet(() -> requestService.save(Request.builder().id(message.getRequestId()).build()));
                    singleTaxiTrip = mapper.messageToSingleTaxiTrip(message);
                    singleTaxiTrip.setTripType(tripType);
                    singleTaxiTrip.setTariff(taxiTariff);
                    singleTaxiTrip.setRequest(request);
                    request.setSingleTaxiTrip(singleTaxiTrip);
                } else {
                    mapper.copyToSingleTaxiTrip(message, singleTaxiTrip);
                }
                try {
                    taxiTripService.save(singleTaxiTrip);
                } catch (DataIntegrityViolationException e) {
                    log.error("Error saving single taxi trip {} with request {}", message.getId(), message.getRequestId(), e);
                }
            } else {
                CoopTaxiTrip coopTaxiTrip = taxiTripService.findCoopTripById(id).orElse(null);
                if (coopTaxiTrip == null) {
                    TaxiTariff taxiTariff = tariffService.findById(message.getTariffId())
                                                         .orElseGet(() ->
                                                                            tariffService.save(
                                                                                    TaxiTariff.builder()
                                                                                              .id(message.getTariffId())
                                                                                              .transportType(TransportTypeEnum.TAXI)
                                                                                              .build()));
                    coopTaxiTrip = mapper.messageToCoopTaxiTrip(message);
                    coopTaxiTrip.setSharedRide(null);
                    coopTaxiTrip.setRideId(message.getSharedRideId());
                    coopTaxiTrip.setTariff(taxiTariff);
                    coopTaxiTrip.setTripType(tripType);
                    final var sharedRide = sharedRideRepository.findById(message.getSharedRideId()).orElse(null);
                    if (sharedRide != null) {
                        coopTaxiTrip.setSharedRide(sharedRide);
                        sharedRide.setCoopTaxiTrip(coopTaxiTrip);
                        sharedRideRepository.save(sharedRide);
                    }
                } else {
                    mapper.copyToCoopTaxiTrip(message, coopTaxiTrip);
                }
                taxiTripService.save(coopTaxiTrip);
            }
        }
    }
}
