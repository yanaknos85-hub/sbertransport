package ru.sber.transport.contractor.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.contractor.database.dao.CargoPackageRepository;
import ru.sber.transport.contractor.database.dao.ContractorRepository;
import ru.sber.transport.contractor.database.model.CargoPackage;
import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.service.CargoPackageService;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of cargo package service
 */
@Service
@Transactional
@RequiredArgsConstructor
class CargoPackageServiceImpl implements CargoPackageService {
    private final CargoPackageRepository repository;
    private final ContractorRepository contractorRepository;
    
    @Override
    public CargoPackage get(UUID contractorId, UUID uuid) {
        return repository.findByIdAndContractor(uuid, contractorId).orElseThrow(() -> new EntityNotFoundException(CargoPackage.class,
                                                                                                  uuid));
    }
    
    @Override
    public List<CargoPackage> getAllPackagesByContractorId(UUID contractorId) {
        checkContractor(contractorId);
        
        return repository.findAllByContractor(contractorId);
    }
    
    @Override
    public CargoPackage save(CargoPackage cargoPackage) {
        checkContractor(cargoPackage.getContractor().getId());
        
        checkDuplicateByLabelAndContractor(cargoPackage.getLabel(), cargoPackage.getContractor().getId());
        
        return repository.save(cargoPackage);
    }
    
    @Override
    public CargoPackage delete(UUID contractorId, UUID uuid) {
        var cargoPackage = get(contractorId, uuid);
        
        cargoPackage.setActive(false);
        return repository.save(cargoPackage);
    }
    
    private void checkContractor(UUID contractorId) {
        if (!contractorRepository.existsById(contractorId)) {
            throw new EntityNotFoundException(Contractor.class, contractorId);
        }
    }
    
    private void checkDuplicateByLabelAndContractor(String label, UUID contractorId) {
        repository.findByLabelAndContractor(label, contractorId)
                  .ifPresent(p -> { throw new DuplicateDataException("CargoPackage", "label",
                                                                     p.getLabel()); });
    }
}
