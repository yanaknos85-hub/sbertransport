package ru.sber.transport.dispatcher.service.file_resolvers.shift;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.AutoparkRepository;
import ru.sber.transport.dispatcher.database.dao.DriverRepository;
import ru.sber.transport.dispatcher.database.dao.VehicleRepository;
import ru.sber.transport.dispatcher.database.model.*;
import ru.sber.transport.dispatcher.dto.files.shift.ShiftFile;
import ru.sber.transport.dispatcher.exceptions.ConflictException;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.dispatcher.service.DispatcherService;
import ru.sber.transport.dispatcher.service.ShiftService;
import ru.sber.transport.dispatcher.validation.ShiftValidator;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.file_works.importer.DataImporter;

import java.util.*;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class ShiftFileResolver implements DataExporter<ShiftFile>, DataImporter<ShiftFile> {

    private final VehicleRepository vehicleRepository;

    private final DriverRepository driverRepository;

    private final ShiftValidator shiftValidator;

    private final ShiftService shiftService;

    private final DispatcherService dispatcherService;

    private final AutoparkRepository autoparkRepository;

    @Value("${dispatcher-room.admin.role:ROLE_DISPATCHER_ROOM_ADMIN}")
    private String dispatcherRoomAdminRole;

    @Value("${dispatcher-room.federal-dispatcher.role:ROLE_FEDERAL_DISPATCHER_CONTRACTOR}")
    private String federalDispatcherRole;

    @Value("${dispatcher-room.manager.role:ROLE_MAIN_DISPATCHER_CONTRACTOR}")
    private String mainDispatcherRole;

    @Override
    public void importData(ShiftFile data, @NotNull Map<String, ?> map, @NotNull JwtAuthenticationToken jwtAuthenticationToken) {
        var driver = driverRepository.findByPersonnelNumberIgnoreCaseAndActiveTrue(data.getPersonnelNumber());
        var vehicle = vehicleRepository.findByStateNumberIgnoreCaseAndInExploitationTrue(data.getStateNumber());
        validateDataAccess(jwtAuthenticationToken, driver, vehicle);
        shiftValidator.validate(data, driver, vehicle);
        var shift = new Shift();
        shift.setDriver(driver.get());
        shift.setVehicle(vehicle.get());
        shift.setContractorId(driver.get().getContractor().getId());
        shift.setStartDate(data.getStartDate().atStartOfDay());
        shift.setEndDate(data.getStartDate().atStartOfDay().plusDays(1).minusSeconds(1));
        shiftService.save(shift, Source.CONTRACTOR);
    }

    @NotNull
    @Override
    public List<ShiftFile> exportData(@NotNull Map<String, ?> params, @NotNull JwtAuthenticationToken jwtAuthenticationToken) {
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private void validateDataAccess(JwtAuthenticationToken token, Optional<Driver> driver, Optional<Vehicle> vehicle) {
        if(driver.isEmpty() || vehicle.isEmpty()){
            return;
        }
        var roles = (Collection<? extends String>) token.getToken().getClaims().get("roles");
        if (roles.contains(dispatcherRoomAdminRole) || roles.contains(federalDispatcherRole)){
            return;
        }
        var id = UUID.fromString(token.getToken().getId());
        var dispatcher = dispatcherService.get(id)
                .orElseGet(() -> dispatcherService.getByOauthId(id)
                        .orElseThrow(() -> new EntityNotFoundException(Dispatcher.class, id)));
        if(!dispatcher.getContractor().getId().equals(driver.get().getContractor().getId())){
            throw new ConflictException(ConflictReason.DRIVER_NOT_BELONGS_TO_USERS_CONTRACTOR.getDescriptionForException());
        }
        var vehicleAutopark = autoparkRepository.findById(vehicle.get().getAutopark().getId())
                .orElseThrow(() -> new EntityNotFoundException(Autopark.class, vehicle.get().getAutopark().getId()));
        if(!dispatcher.getContractor().getId().equals(vehicleAutopark.getContractor().getId())){
            throw new ConflictException(ConflictReason.VEHICLE_NOT_BELONGS_TO_USERS_CONTRACTOR.getDescriptionForException());
        }
        if(!roles.contains(mainDispatcherRole)){
            if(dispatcher.getAutopark() == null){
                throw new ConflictException(ConflictReason.USER_NOT_BELONGS_TO_CONTRACTORS_AUTOPARK.getDescriptionForException());
            }
            if(driver.get().getAutopark() != null && !dispatcher.getAutopark().getId().equals(driver.get().getAutopark().getId())){
                throw new ConflictException(ConflictReason.DRIVER_NOT_BELONGS_TO_USERS_AUTOPARK.getDescriptionForException());
            }
            if(!dispatcher.getAutopark().getId().equals(vehicleAutopark.getId())){
                throw new ConflictException(ConflictReason.VEHICLE_NOT_BELONGS_TO_USERS_AUTOPARK.getDescriptionForException());
            }
        }
    }
}
