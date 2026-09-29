package ru.sber.transport.contractor.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.service.IntegrationService;
import ru.sber.transport.contractor.service.IntegrationServiceFactory;
import ru.sber.transport.contractor.service.impl.integration.DefaultIntegrationServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class IntegrationServiceFactoryImpl implements IntegrationServiceFactory {

    private final Map<ContractorType, IntegrationService> serviceMap;


    public IntegrationServiceFactoryImpl(@Autowired List<IntegrationService> services) {
        this.serviceMap = services.stream().collect(Collectors.toMap(IntegrationService::getType, Function.identity()));
    }

    @Override
    public IntegrationService getService(ContractorType contractorType) {
        return Optional.ofNullable(serviceMap.get(contractorType))
                .orElse(new DefaultIntegrationServiceImpl());
    }
}
