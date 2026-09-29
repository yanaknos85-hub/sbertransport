package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.database.dao.ContractorRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.service.ContractorService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Имплементация сервиса для работы с контрагентами исполнителями поездок на такси
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ContractorServiceImpl implements ContractorService {
    
    private final ContractorRepository contractorRepository;
    
    @Override
    public void softDelete(Contractor contractor) {
        contractor.setDeleted(true);
        contractorRepository.save(contractor);
    }
    
    @Override
    public Optional<Contractor> getOptional(UUID contractorId) {
        return contractorRepository.findById(contractorId);
    }
    
    @Override
    public Contractor get(UUID contractorId) {
        return getOptional(contractorId).orElseThrow(() ->
                new EntityNotFoundException(Contractor.class, contractorId));
    }

    @Override
    public List<Contractor> getAll(Collection<UUID> contractorIds) {
        return contractorRepository.findAllById(contractorIds);
    }

    @Override
    public Map<UUID, Contractor> getAllByRequestIds(Collection<UUID> requestIds) {
        return  Stream.concat(
                        contractorRepository.findAllByRequestForCarsh(requestIds).stream(),
                        contractorRepository.findAllByRequestForTaxi(requestIds).stream())
                .collect(Collectors.toMap(Contractor::getId, contractor -> contractor));
    }

    @Override
    public void saveOrUpdate(Contractor contractor) {
        contractorRepository.save(contractor);
    }
    
}
