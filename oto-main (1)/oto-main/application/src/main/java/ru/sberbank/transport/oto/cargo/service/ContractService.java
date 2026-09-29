package ru.sberbank.transport.oto.cargo.service;

import ru.sberbank.transport.oto.cargo.database.model.tariff.Contract;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ContractService {
    Optional<Contract> findById(UUID id);
    
    Contract save(Contract contractor);
    
    void deactivate(UUID id);
    
    Set<UUID> findAllContractorIds();
}
