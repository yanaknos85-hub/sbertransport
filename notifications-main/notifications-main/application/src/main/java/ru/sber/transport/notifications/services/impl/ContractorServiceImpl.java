package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.dao.messages.contractor.ContractorRepository;
import ru.sber.transport.notifications.database.model.contractor.Contractor;
import ru.sber.transport.notifications.services.ContractorService;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContractorServiceImpl implements ContractorService {
    
    private final ContractorRepository contractorRepository;
    
    @Override
    public Optional<Contractor> get(UUID id) {
        return contractorRepository.findById(id);
    }
    
    @Override
    public Contractor save(Contractor contractor) {
        return contractorRepository.save(contractor);
    }

    @Override
    public void deleteById(UUID id) {
        contractorRepository.deleteById(id);
    }
}
