package ru.sberbank.ditsib.transport.vehicle.service.validation.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.vehicle.database.dao.FuelTypeNameRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.FuelTypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.exception.FuelTypeNameAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.service.validation.FuelTypeValidationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FuelTypeValidationServiceImpl implements FuelTypeValidationService {

    private final FuelTypeRepository fuelTypeRepository;
    private final FuelTypeNameRepository fuelTypeNameRepository;

    @Override
    public void checkIfFuelTypeAlreadyExists(String title, EngineType engineType) {
        fuelTypeRepository.findByTitleAndEngineType(title, engineType)
                .ifPresent(entity -> {
                    throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                });
    }

    @Override
    public void checkFuelTypeNamesAlreadyInUse(List<String> fuelTypeNames, UUID fuelTypeId) {
        var existingList = fuelTypeNameRepository.findByNameIgnoreCaseIn(fuelTypeNames);
        if (!existingList.isEmpty()) {
            var names = new ArrayList<String>(existingList.size());
            var fuelTypeIds = new ArrayList<UUID>(existingList.size());
            existingList.forEach(it -> {
                names.add(it.getName());
                fuelTypeIds.add(it.getFuelTypeId());
            });
            throw new FuelTypeNameAlreadyExistsException(names, fuelTypeIds);
        }
    }
}
