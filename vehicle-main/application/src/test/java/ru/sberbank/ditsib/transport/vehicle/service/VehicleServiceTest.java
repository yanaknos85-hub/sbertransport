package ru.sberbank.ditsib.transport.vehicle.service;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.vehicle.database.dao.*;
import ru.sberbank.ditsib.transport.vehicle.database.model.*;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleRequestDto;
import ru.sberbank.ditsib.transport.vehicle.mapper.VehicleMapper;
import ru.sberbank.ditsib.transport.vehicle.service.impl.VehicleServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {
    
    @Spy
    private VehicleMapper mapper = Mappers.getMapper(VehicleMapper.class);
    
    @InjectMocks
    private VehicleServiceImpl vehicleService;
    
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ModelRepository modelRepository;
    @Mock
    private FuelTypeRepository fuelTypeRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private DriveRepository driveRepository;
    @Mock
    private TransmissionTypeRepository transmissionTypeRepository;
    @Mock
    private BodyTypeRepository bodyTypeRepository;
    @Mock
    private WheelSizeRepository wheelSizeRepository;
    @Mock
    private EngineTypeRepository engineTypeRepository;
    
    @Test
    @DisplayName("Добавление автомобиля должно происходить успешно")
    void addVehicle() {
        var engineType = Instancio.create(EngineType.class);
        var fuelType = Instancio.create(FuelType.class).setEngineType(engineType);
        var baseRequestDto = Instancio.create(VehicleRequestDto.class);
        var vehicleRequestDto = baseRequestDto.toBuilder()
                .engineTypeId(engineType.getId())
                .build();
        
        when(modelRepository.findById(vehicleRequestDto.modelId())).thenReturn(Optional.of(Instancio.create(Model.class)));
        when(fuelTypeRepository.findAllById(vehicleRequestDto.fuelTypeIds())).thenReturn(List.of(fuelType));
        when(engineTypeRepository.findById(any())).thenReturn(Optional.of(engineType));
        when(categoryRepository.findById(vehicleRequestDto.categoryId())).thenReturn(Optional.of(Instancio.create(Category.class)));
        when(driveRepository.findById(vehicleRequestDto.driveId())).thenReturn(Optional.of(Instancio.create(Drive.class)));
        when(transmissionTypeRepository.findById(vehicleRequestDto.transmissionTypeId())).thenReturn(Optional.of(Instancio.create(TransmissionType.class)));
        when(bodyTypeRepository.findById(vehicleRequestDto.bodyTypeId())).thenReturn(Optional.of(Instancio.create(BodyType.class)));
        when(wheelSizeRepository.findById(vehicleRequestDto.frontWheelSizeId())).thenReturn(Optional.of(Instancio.create(WheelSize.class)));
        when(wheelSizeRepository.findById(vehicleRequestDto.rearWheelSizeId())).thenReturn(Optional.of(Instancio.create(WheelSize.class)));
        when(vehicleRepository.saveAndFlush(any())).thenReturn(Instancio.create(Vehicle.class));

        vehicleService.create(vehicleRequestDto);

        verify(vehicleRepository, times(1)).saveAndFlush(any());
    }
    
    @Test
    @DisplayName("Проверка удаления запси")
    void deleteRecord() {
        var vehicle = Instancio.create(Vehicle.class);
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        
        vehicleService.delete(vehicle.getId());

        verify(vehicleRepository, times(1)).delete(any(Vehicle.class));
    }
}
