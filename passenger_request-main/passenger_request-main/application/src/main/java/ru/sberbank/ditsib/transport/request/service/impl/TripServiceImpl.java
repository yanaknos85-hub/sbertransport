package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.request.messaging.message.RequestFactDataMessage;
import ru.sberbank.ditsib.transport.request.database.dao.CoopTaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.dao.SingleTaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.dao.TaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.model.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.request.service.TripService;

import java.util.Optional;
import java.util.UUID;

/**
 * Имплементация сервиса взаимодействия с исполнителями поездок на такси
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {
    
    private final CoopTaxiTripRepository coopTaxiTripRepository;
    
    private final SingleTaxiTripRepository singleTaxiTripRepository;
    
    private final TaxiTripRepository taxiTripRepository;
    
    @Override
    public Optional<SingleTaxiTrip> getSingleTripByRequestId(UUID requestId) {
        return singleTaxiTripRepository.findByRequestId(requestId);
    }
    
    @Override
    public Optional<CoopTaxiTrip> getCoopTripByRideId(UUID rideId) {
        return coopTaxiTripRepository.findByRideId(rideId);
    }
    
    @Override
    public void updateFactData(RequestFactDataMessage message) {
        taxiTripRepository.updateFactDataByHrId(
                message.getFactTotalWaitingTime(),
                message.getRegistryHrId(),
                message.getFactCost(),
                message.getFactDistance(),
                message.getIsPaid(),
                message.getHrId());
    }
}
