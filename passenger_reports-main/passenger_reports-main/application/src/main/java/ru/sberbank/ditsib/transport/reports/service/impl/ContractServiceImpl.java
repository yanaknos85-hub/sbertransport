package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.ContractRepository;
import ru.sberbank.ditsib.transport.reports.model.tariff.Contract;
import ru.sberbank.ditsib.transport.reports.service.ContractService;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContractServiceImpl implements ContractService {
    
    private final ContractRepository contractRepository;
    
    @Override
    public Optional<Contract> findById(UUID id) {
        return contractRepository.findById(id);
    }
    
    @Override
    public Contract save(Contract contract) {
        return contractRepository.save(contract);
    }
    
    @Override
    public void deactivate(UUID id) {
        contractRepository.deactivate(id);
    }
    
    @Override
    public Set<UUID> findAllContractorIds() {
        return contractRepository.findAllContractorIds();
    }
}
