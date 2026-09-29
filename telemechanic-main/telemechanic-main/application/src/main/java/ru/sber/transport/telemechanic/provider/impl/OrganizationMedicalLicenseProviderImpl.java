package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.database.model.OrganizationMedicalLicense;
import ru.sber.transport.telemechanic.messaging.listener.message.OrganizationMedicalLicenseMessage;
import ru.sber.transport.telemechanic.provider.OrganizationMedicalLicenseProvider;
import ru.sber.transport.telemechanic.service.OrganizationMedicalLicenseService;

@RequiredArgsConstructor
@Component
public class OrganizationMedicalLicenseProviderImpl implements OrganizationMedicalLicenseProvider {
    
    private final OrganizationMedicalLicenseService service;
    
    @Override
    public void save(OrganizationMedicalLicenseMessage message) {
        service.save(new OrganizationMedicalLicense(message.id(),
                                                    message.series(),
                                                    message.number(),
                                                    message.issueDate(),
                                                    message.expiryDate(),
                                                    true));
    }
}
