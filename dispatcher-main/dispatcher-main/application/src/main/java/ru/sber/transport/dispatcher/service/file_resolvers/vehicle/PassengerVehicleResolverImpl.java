package ru.sber.transport.dispatcher.service.file_resolvers.vehicle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;
import ru.sber.transport.dispatcher.dto.files.vehicle.PassengerVehicleFile;
import ru.sber.transport.dispatcher.mappers.VehicleFileMapper;
import ru.sber.transport.dispatcher.service.AuthCheckService;
import ru.sber.transport.dispatcher.service.AutoparkService;
import ru.sber.transport.dispatcher.service.VehicleService;

@Slf4j
@Component
public class PassengerVehicleResolverImpl extends VehicleFileResolver<PassengerVehicleFile> {

    public PassengerVehicleResolverImpl(AuthCheckService authCheckService, VehicleService vehicleService,
                                        AutoparkService autoparkService, VehicleFileMapper vehicleFileMapper) {
        super(authCheckService, vehicleService, autoparkService, vehicleFileMapper, VehicleType.PASSENGER);
    }
}
