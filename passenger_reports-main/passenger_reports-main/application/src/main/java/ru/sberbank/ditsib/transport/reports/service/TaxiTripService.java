package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxiTripService {
    
    List<CoopTaxiTrip> findAllBySharedRideIds(List<UUID> sharedRideIds);
    
    Optional<CoopTaxiTrip> findBySharedRideId(UUID sharedRideId);
    
    List<SingleTaxiTrip> findAllByRequestIds(List<UUID> requestIds);
    
    Optional<CoopTaxiTrip> findCoopTripById(UUID id);
    
    Optional<SingleTaxiTrip> findSingleTripById(UUID id);

    SingleTaxiTrip save(SingleTaxiTrip singleTaxiTrip);
    
    CoopTaxiTrip save(CoopTaxiTrip coopTaxiTrip);
    
    Optional<TaxiTrip> findById(UUID id);
    
    void delete(TaxiTrip taxiTrip);
    
    TaxiTrip save(TaxiTrip taxiTrip);
}
