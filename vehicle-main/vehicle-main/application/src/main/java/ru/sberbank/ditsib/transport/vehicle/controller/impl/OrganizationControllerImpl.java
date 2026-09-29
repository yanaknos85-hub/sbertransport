package ru.sberbank.ditsib.transport.vehicle.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.vehicle.controller.OrganizationController;
import ru.sberbank.ditsib.transport.vehicle.dto.GetDepartmentsInfo;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationDto;
import ru.sberbank.ditsib.transport.vehicle.helper.UserAuthorizationHelper;
import ru.sberbank.ditsib.transport.vehicle.service.corp.OrganizationService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrganizationControllerImpl implements OrganizationController {
    
    private final OrganizationService organizationService;
    
    @Override
    public List<OrganizationDto> getAll(Authentication authentication) {
        return organizationService.getAll();
    }
    
    @Override
    public List<GetDepartmentsInfo> getAllWithDepartment(Set<UUID> request) {
        return organizationService.getAllWithDepartment(request);
    }
    
    @Override
    public OrganizationDto get(Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return organizationService.getByUserId(userId);
    }
}
