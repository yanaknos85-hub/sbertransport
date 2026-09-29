package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.model.EwbTariff;
import ru.sber.transport.telemechanic.messaging.listener.message.EwbTariffMessage;
import ru.sber.transport.telemechanic.provider.EwbTariffProvider;
import ru.sber.transport.telemechanic.service.EwbTariffService;

@Transactional
@RequiredArgsConstructor
@Component
public class EwbTariffProviderImpl implements EwbTariffProvider {
    
    private final EwbTariffService service;
    
    @Override
    public void save(EwbTariffMessage message) {
        service.save(new EwbTariff(message.tariffId(),
                                   new EwbContract().setContractId(message.contractId()),
                                   message.organizationId(),
                                   message.departmentId(),
                                   message.active()));
    }
}
