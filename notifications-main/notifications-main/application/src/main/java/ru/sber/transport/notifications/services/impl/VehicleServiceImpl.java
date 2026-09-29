package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.dao.VehicleRepository;
import ru.sber.transport.notifications.database.model.request.Vehicle;
import ru.sber.transport.notifications.services.VehicleService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    
    private final VehicleRepository vehicleRepository;
    
    @Override
    public Optional<Vehicle> get(UUID driverId) {
        return vehicleRepository.findById(driverId);
    }
    
    @Override
    public Vehicle save(Vehicle driver) {
        return vehicleRepository.save(driver);
    }
    
    @Override
    public void deleteById(UUID driverId) {
        vehicleRepository.deleteById(driverId);
    }
}
