package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.transport.oto.cargo.database.dao.ContractorRepository;
import ru.sberbank.transport.oto.cargo.database.model.Contractor;
import ru.sberbank.transport.oto.cargo.service.ContractorService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContractorServiceImpl implements ContractorService {
    
    private final ContractorRepository contractorRepository;
    
    @Override
    public Optional<Contractor> findById(UUID id) {
        return contractorRepository.findById(id);
    }
    
    @Override
    public void delete(Contractor contractor) {
        contractor.setActive(false);
        contractorRepository.save(contractor);
    }
    
    @Override
    public Contractor save(Contractor contractor) {
        return contractorRepository.save(contractor);
    }
    
    @Override
    public Contractor findOrCreateContractorById(UUID contractorId) {
        var contractor = findById(contractorId);
        return contractor.orElseGet(() -> save(Contractor.builder().id(contractorId).build()));
    }
    
    @Override
    public List<Contractor> findAll() {
        return contractorRepository.findAll();
    }
}
