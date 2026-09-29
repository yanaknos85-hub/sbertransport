package ru.sber.transport.dispatcher.controller.impl;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.dispatcher.controller.DriverController;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.dto.DriverDTO;
import ru.sber.transport.dispatcher.dto.NewDriverDTO;
import ru.sber.transport.dispatcher.dto.PatchDataV2;
import ru.sber.transport.dispatcher.dto.VehicleDTO;
import ru.sber.transport.dispatcher.dto.enums.PatchField;
import ru.sber.transport.dispatcher.dto.search.DriverSearchDTO;
import ru.sber.transport.dispatcher.mappers.DriverMapper;
import ru.sber.transport.dispatcher.service.ContractorService;
import ru.sber.transport.dispatcher.service.DriverService;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of drivers controller.
 */
@RequiredArgsConstructor
@RestController
@Transactional
class DriverControllerImpl implements DriverController {

    private final DriverService driverService;
    private final ContractorService contractorService;
    private final DriverMapper driverMapper;

    @Override
    public DriverDTO add(UUID contractorId, @Valid NewDriverDTO drivers) {
        return driverService.add(contractorId, drivers);
    }
    
    @Override
    public void edit(UUID contractorId, UUID driversId, @Valid NewDriverDTO drivers) {
        driverService.edit(contractorId, driversId, drivers);
    }

    @Override
    public void delete(UUID contractorId, UUID driverId) {
        driverService.delete(contractorId, driverId);
    }

    @Override
    public DriverDTO get(UUID contractorId, UUID driversId) {
        return driverService.get(contractorId, driversId);
    }
    
    @Override
    public Page<DriverDTO> getAll(UUID contractorId, DriverSearchDTO driverSearchDTO) {
        if (!contractorService.isContractorExists(contractorId)) {
            throw new EntityNotFoundException(Contractor.class, contractorId);
        }
        return driverService.get(contractorId, driverSearchDTO);
    }

    @Override
    public DriverDTO getSelfProfile(JwtAuthenticationToken authentication) {
        var driver = getDriver(authentication);
        return driverMapper.toDto(driver);
    }

    @Override
    public void signPdn(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        driverService.signPdn(userId);
    }

    @Override
    public void patchDispatcher(List<PatchDataV2> data, Authentication authentication) {
        var authenticated = UUID.fromString(((JwtAuthenticationToken) authentication).getToken().getId());
        driverService.patchDriver(authenticated, data.stream().collect(Collectors.toMap(PatchDataV2::field, PatchDataV2::value)));
    }

    @Override
    public VehicleDTO getSelfVehicle(JwtAuthenticationToken authentication) {
        var driver = getDriver(authentication);
        return driverService.getSelfVehicle(driver);
    }

    @Override
    public void patch(UUID contractorId, UUID driverId, List<PatchDataV2> data) {
        driverService.patchDriver(driverId, PatchField.getPatchDataMap(data));
    }

    private Driver getDriver(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return driverService.get(userId)
                .orElseGet(() -> driverService.findByOauthId(userId)
                        .orElseThrow(() -> new EntityNotFoundException(Driver.class, userId)));
    }
}
