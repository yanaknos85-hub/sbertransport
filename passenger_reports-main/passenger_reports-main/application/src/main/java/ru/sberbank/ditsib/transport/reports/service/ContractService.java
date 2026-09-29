package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.tariff.Contract;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ContractService {
    Optional<Contract> findById(UUID id);
    
    Contract save(Contract contractor);
    
    void deactivate(UUID id);
    
    Set<UUID> findAllContractorIds();
}
