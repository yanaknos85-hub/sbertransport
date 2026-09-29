package ru.sber.transport.dispatcher.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.dto.NewVehicleDTO;
import ru.sber.transport.dispatcher.dto.VehicleDTO;
import ru.sber.transport.dispatcher.dto.files.vehicle.CargoVehicleFile;
import ru.sber.transport.dispatcher.dto.files.vehicle.PassengerVehicleFile;
import ru.sber.transport.dispatcher.dto.files.vehicle.VehicleFile;

@Component
@RequiredArgsConstructor
public class VehicleFileMapper {

    private final VehicleMapper vehicleMapper;

    @SuppressWarnings("unchecked")
    public <T extends VehicleFile> T toVehicleFile(VehicleDTO vehicleDTO) {
        return switch (vehicleDTO.getVehicleType()) {
            case CARGO, UNIVERSAL -> (T) vehicleMapper.toCargoVehicleFile(vehicleDTO);
            case PASSENGER -> (T) vehicleMapper.toPassengerVehicleFile(vehicleDTO);
            case null -> null;
        };
    }

    public <T extends VehicleFile> NewVehicleDTO toNewVehicleDto(T vehicleFile) {
        if (vehicleFile instanceof CargoVehicleFile cargoVehicleFile) {
            return vehicleMapper.toVehicleDto(cargoVehicleFile);
        } else if (vehicleFile instanceof PassengerVehicleFile passengerVehicleFile) {
            return vehicleMapper.toVehicleDto(passengerVehicleFile);
        } else {
            throw new IllegalArgumentException("Class " + vehicleFile.getClass().getName() + " is not supported");
        }
    }

}
