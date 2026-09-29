package ru.sber.transport.dispatcher.controller.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.dispatcher.controller.VehicleController;
import ru.sber.transport.dispatcher.dto.NewVehicleDTO;
import ru.sber.transport.dispatcher.dto.VehicleDTO;
import ru.sber.transport.dispatcher.dto.search.VehicleSearchDTO;
import ru.sber.transport.dispatcher.service.VehicleService;

import java.util.UUID;

/**
 * Implementation of transport controller.
 */
@RequiredArgsConstructor
@RestController
class VehicleControllerImpl implements VehicleController {
    
    private final VehicleService vehicleService;
    
    @Override
    public VehicleDTO add(UUID contractorId, UUID autoparkId, @Valid NewVehicleDTO vehicleDTO) {
        return vehicleService.add(contractorId, autoparkId, vehicleDTO);
    }
    
    @Override
    public void edit(UUID contractorId, UUID autoparkId, UUID vehicleId, @Valid NewVehicleDTO vehicleDTO) {
        vehicleService.edit(contractorId, autoparkId, vehicleId, vehicleDTO);
    }
    
    @Override
    public void delete(UUID contractorId, UUID autoparkId, UUID vehicleId) {
        vehicleService.delete(contractorId, autoparkId, vehicleId);
    }
    
    @Override
    public VehicleDTO get(UUID contractorId, UUID autoparkId, UUID transportId) {
        return vehicleService.get(contractorId, autoparkId, transportId);
    }
    
    @Override
    public Page<VehicleDTO> getAll(UUID contractorId, UUID autoparkId, VehicleSearchDTO searchDTO) {
        return vehicleService.getAllPageable(contractorId, autoparkId, searchDTO);
    }
}
