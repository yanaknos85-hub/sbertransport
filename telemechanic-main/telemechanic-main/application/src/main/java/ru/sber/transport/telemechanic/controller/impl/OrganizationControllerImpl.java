package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.OrganizationController;
import ru.sber.transport.telemechanic.dto.OrganizationDto;
import ru.sber.transport.telemechanic.dto.OrganizationWithDepartmentDto;
import ru.sber.transport.telemechanic.dto.TariffDepartmentResponse;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.service.OrganizationService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@E2EController
@RequiredArgsConstructor
public class OrganizationControllerImpl implements OrganizationController {
    private final OrganizationService organizationService;
    
    @Override
    public OrganizationDto get(@E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return organizationService.getByUserId(userId);
    }
    
    @Override
    public List<OrganizationDto> getAll() {
        return organizationService.getAll();
    }
    
    @Override
    public List<OrganizationDto> getAllWithInternalContractor() {
            return organizationService.getAllWithInternalContractor();
    }
    
    @Override
    public List<OrganizationWithDepartmentDto> getAllWithDepartment(Set<UUID> uuids) {
        return organizationService.getAllWithDepartment(uuids);
    }
    
    @Override
    public List<TariffDepartmentResponse> getAllDepartmentWithTariff(UUID id) {
        return organizationService.getAllDepartmentWithTariff(id);
    }
}
