package ru.sberbank.ditsib.transport.tariff.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractorRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.ContractType;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.ContractorService;

import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса контрагентов.
 */
@RequiredArgsConstructor
@Component
@Transactional
class ContractorServiceImpl implements ContractorService {
    
    private final ContractorRepository contractorRepository;
    
    private final ContractService contractService;
    
    @Override
    public void delete(Contractor contractor) {
        contractorRepository.delete(contractor);
        contractService.getByContractor(contractor.getId())
                       .forEach(contractService::delete);
    }
    
    @Override
    public Optional<Contractor> get(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return contractorRepository.findById(id);
    }
    
    @Override
    public Optional<Contractor> get(String name) {
        return contractorRepository.findByName(name);
    }
    
    @Override
    public void save(Contractor contractor) {
        contractorRepository.save(contractor);
    }
    
    @Override
    @Transactional
    public void updateContractorsRegionIds(Contract contract) {
        if (contract.getContractType().equals(ContractType.INCOME)) {
            return;
        }
        
        Optional<Contractor> optionalContractorById = get(contract.getContractorId());
        var contractor = optionalContractorById
                .orElseThrow(() -> new EntityNotFoundException(Contractor.class, contract.getContractorId()));
        List<UUID> listOfRegionIds = new ArrayList<>(contract.getRegionIds());
        Objects.requireNonNull(contractor).getRegionIds().addAll(listOfRegionIds);
        save(contractor);
    }
    
    @Override
    public Map<UUID, String> getNames(Collection<UUID> values) {
        if (values.isEmpty()) {
            return new HashMap<>();
        }
        return contractorRepository.findAllById(values).stream()
                .collect(Collectors.toMap(Contractor::getId, Contractor::getName));
    }
}
