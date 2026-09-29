package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.transport.oto.cargo.database.dao.ContractRepository;
import ru.sberbank.transport.oto.cargo.database.model.tariff.Contract;
import ru.sberbank.transport.oto.cargo.service.ContractService;

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
