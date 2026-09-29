package ru.sberbank.ditsib.transport.vehicle.validation;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.vehicle.database.dao.FuelTypeNameRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.FuelTypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelType;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelTypeName;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.exception.FuelTypeNameAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.service.validation.impl.FuelTypeValidationServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class FuelTypeValidationServiceImplTest {

    @InjectMocks
    private FuelTypeValidationServiceImpl fuelTypeValidationService;
    @Mock
    private FuelTypeRepository fuelTypeRepository;
    @Mock
    private FuelTypeNameRepository fuelTypeNameRepository;

    @Test
    void checkIfFuelTypeAlreadyExists() {
        var fuelType = Instancio.create(FuelType.class);
        var title = fuelType.getTitle();
        var engineType = fuelType.getEngineType();
        doReturn(Optional.of(fuelType)).when(fuelTypeRepository).findByTitleAndEngineType(title, engineType);
        assertThrows(EntityAlreadyExistsException.class,
                () -> fuelTypeValidationService.checkIfFuelTypeAlreadyExists(title, engineType));
    }

    @Test
    void checkFuelTypeNamesAlreadyInUse() {
        var fuelTypeId = UUID.randomUUID();
        var fuelTypeNames = Instancio.createList(String.class);
        doReturn(Instancio.createList(FuelTypeName.class)).when(fuelTypeNameRepository).findByNameIgnoreCaseIn(fuelTypeNames);
        assertThrows(FuelTypeNameAlreadyExistsException.class,
                () -> fuelTypeValidationService.checkFuelTypeNamesAlreadyInUse(fuelTypeNames, fuelTypeId));
    }

}
