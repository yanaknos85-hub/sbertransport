package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.DriverController;
import ru.sber.transport.telemechanic.dto.driver.*;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.service.DriverService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.util.UUID;

@RestController
@E2EController
@RequiredArgsConstructor
public class DriverControllerImpl implements DriverController {
    
    private final DriverService driverService;
    
    @Override
    public void addDriver(AddDriverRequest request) {
        driverService.addDriver(request);
    }
    
    @Override
    public void editDriver(UUID id, EditDriverRequest request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        driverService.editDriver(id, request, userId);
    }
    
    @Override
    public void deactivateDriver(UUID id) {
        driverService.deactivateDriver(id);
    }
    
    @Override
    public GetDriverResponse getDriverById(UUID id) {
        return driverService.getDriverById(id);
    }
    
    @Override
    public Page<DriverByFioResponse> getDrivers(DriverFilters filters) {
        return driverService.getDrivers(filters);
    }
    
    @Override
    public Page<DriverByFioResponse> getDriversByFio(DriverByFioRequest request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return driverService.getDriversByFio(request, userId);
    }
    
    @Override
    public Page<DriverSearchResponse> search(DriverSearchRequest request) {
        return driverService.search(request);
    }
}
