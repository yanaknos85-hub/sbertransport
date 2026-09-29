package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.MedicRequestController;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestRegistrySelfOrganizationRequest;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.service.MedicRequestService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.util.Map;

@RestController
@E2EController
@RequiredArgsConstructor
public class MedicRequestControllerImpl implements MedicRequestController {
    
    private final MedicRequestService medicRequestService;
    
    @Override
    public Page<Map<String, Object>> getMedicRequestRegistryForAllOrganizations(
            MedicRequestRegistryAllOrganizationsRequest request
                                                                               ) {
        return medicRequestService.searchRegistryForAllOrganizations(request);
    }
    
    @Override
    public Page<Map<String, Object>> getMedicRequestRegistryForSelfOrganization(
            MedicRequestRegistrySelfOrganizationRequest request, @E2EUser("principal") Authentication authentication
                                                                               ) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return medicRequestService.searchRegistryForSelfOrganization(request, userId);
    }
}
