package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.CoopTaxiTripRepository;
import ru.sberbank.ditsib.transport.reports.dao.SingleTaxiTripRepository;
import ru.sberbank.ditsib.transport.reports.dao.TaxiTripRepository;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.service.TaxiTripService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaxiTripServiceImpl implements TaxiTripService {
    private final SingleTaxiTripRepository singleRepository;
    private final CoopTaxiTripRepository coopRepository;
    private final TaxiTripRepository repository;
    
    @Override
    public List<CoopTaxiTrip> findAllBySharedRideIds(List<UUID> rideIds) {
        return coopRepository.findAllBySharedRideIds(rideIds);
    }
    
    @Override
    public Optional<CoopTaxiTrip> findBySharedRideId(UUID rideId) {
        return coopRepository.findBySharedRideId(rideId);
    }
    
    @Override
    public List<SingleTaxiTrip> findAllByRequestIds(List<UUID> requestIds) {
        return singleRepository.findAllByRequestIds(requestIds);
    }
    
    @Override
    public Optional<CoopTaxiTrip> findCoopTripById(UUID id) {
        return coopRepository.findById(id);
    }
    
    @Override
    public Optional<SingleTaxiTrip> findSingleTripById(UUID id) {
        return singleRepository.findById(id);
    }
    
    @Override
    public SingleTaxiTrip save(SingleTaxiTrip singleTaxiTrip) {
        return singleRepository.save(singleTaxiTrip);
    }
    
    @Override
    public CoopTaxiTrip save(CoopTaxiTrip coopTaxiTrip) {
        return coopRepository.saveAndFlush(coopTaxiTrip);
    }
    
    @Override
    public Optional<TaxiTrip> findById(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public void delete(TaxiTrip taxiTrip) {
        repository.delete(taxiTrip);
    }
    
    @Override
    public TaxiTrip save(TaxiTrip taxiTrip) {
       return repository.save(taxiTrip);
    }
    
}
