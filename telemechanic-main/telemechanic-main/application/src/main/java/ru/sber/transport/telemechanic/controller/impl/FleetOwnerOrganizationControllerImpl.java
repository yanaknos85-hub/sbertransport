package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.FleetOwnerOrganizationController;
import ru.sber.transport.telemechanic.dto.GetAllActiveOrganizationNamesDto;
import ru.sber.transport.telemechanic.service.FleetOwnerOrganizationService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;

import java.util.List;

@RestController
@E2EController
@RequiredArgsConstructor
public class FleetOwnerOrganizationControllerImpl implements FleetOwnerOrganizationController {
    private final FleetOwnerOrganizationService fleetOwnerOrganizationService;
    
    @Override
    public List<GetAllActiveOrganizationNamesDto> getFleetOwnerOrganizations() {
        return fleetOwnerOrganizationService.getAllActive();
    }
}
