package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.OrganizationMedicalLicenseController;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.service.OrganizationMedicalLicenseService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

@RestController
@E2EController
@RequiredArgsConstructor
public class OrganizationMedicalLicenseControllerImpl implements OrganizationMedicalLicenseController {
    
    private final OrganizationMedicalLicenseService organizationMedicalLicenseService;
    
    @Override
    public void checkMedicalLicense(@E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        organizationMedicalLicenseService.checkMedicalLicense(userId);
    }
}
