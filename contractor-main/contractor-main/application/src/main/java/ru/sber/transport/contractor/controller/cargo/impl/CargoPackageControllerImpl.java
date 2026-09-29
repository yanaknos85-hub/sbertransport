package ru.sber.transport.contractor.controller.cargo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.contractor.database.model.CargoPackage;
import ru.sber.transport.contractor.mappers.CargoPackageMapper;
import ru.sber.transport.contractor.messaging.senders.CargoPackageSender;
import ru.sber.transport.contractor.service.CargoPackageService;
import ru.sber.transport.contractor.controller.cargo.CargoPackageController;
import ru.sber.transport.contractor.dto.cargo.CargoPackageDto;
import ru.sber.transport.contractor.dto.cargo.NewCargoPackageDto;

import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
class CargoPackageControllerImpl implements CargoPackageController {
    private final CargoPackageService service;
    private final CargoPackageSender sender;
    private final CargoPackageMapper mapper;
    
    @Override
    public CargoPackageDto getPackage(UUID contractorId, UUID packageId) {
        return mapper.toDto(service.get(contractorId, packageId));
    }
    
    @Override
    public CargoPackageDto addPackage(UUID contractorId, NewCargoPackageDto newData) {
        newData.setContractor(contractorId);

        var pkg = new CargoPackage();

        mapper.update(pkg, newData);
        
        var cargoPackage = service.save(pkg);
        
        sender.send(cargoPackage);
        
        return mapper.toDto(cargoPackage);
    }
    
    @Override
    public void updatePackage(UUID contractorId, UUID packageId, NewCargoPackageDto newData) {
       var deleted = service.delete(contractorId, packageId);
        
        newData.setContractor(contractorId);

        var pkg = new CargoPackage();

        mapper.update(pkg, newData);

        var cargoPackage = service.save(pkg);

        sender.send(deleted);
        sender.send(cargoPackage);
    }
    
    @Override
    public void deletePackage(UUID contractorId, UUID packageId) {
        var deleted = service.delete(contractorId, packageId);
        
        sender.send(deleted);
    }
    
    @Override
    public Collection<CargoPackageDto> getPackages(UUID contractorId) {
        return service.getAllPackagesByContractorId(contractorId)
                      .stream()
                      .map(mapper::toDto)
                .toList();
    }
}
