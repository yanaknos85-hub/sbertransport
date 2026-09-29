package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;
import ru.sber.transport.telemechanic.messaging.listener.message.EwbContractMessage;
import ru.sber.transport.telemechanic.provider.EwbContractProvider;
import ru.sber.transport.telemechanic.service.EwbContractService;
import ru.sber.transport.telemechanic.service.OrganizationMedicalLicenseService;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class EwbContractProviderImpl implements EwbContractProvider {
    
    private final OrganizationMedicalLicenseService organizationMedicalLicenseService;
    private final EwbContractService ewbContractService;
    
    @Override
    public void save(EwbContractMessage message) {
        if (message.organizationMedicalLicenseId() != null
            && !organizationMedicalLicenseService.existsAndActive(message.organizationMedicalLicenseId())) {
            var errorMessage = String.format("Can't save ewb contract, id:%s, organizationMedicalLicenseId:%s, organization medical license isn't present",
                                             message.getId(),
                                             message.organizationMedicalLicenseId());
            log.info(errorMessage);
            throw new AwaitingSynchronizationException(errorMessage);
        } else {
            ewbContractService.save(new EwbContract(message.contractId(),
                                                    message.organizationId(),
                                                    message.inspectionType(),
                                                    message.edfOperatorId(),
                                                    message.edfCode(),
                                                    message.organizationMedicalLicenseId(),
                                                    message.active(),
                                                    message.start(),
                                                    message.end()));
        }
    }
}
