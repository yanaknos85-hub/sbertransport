package ru.sber.transport.contractor.service.impl.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.contractor.config.IntegrationConfig;
import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.feign.InternalClient;
import ru.sber.transport.contractor.mappers.ContractorMapper;
import ru.sber.transport.contractor.service.IntegrationService;

import java.net.URI;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AutoserviceInternalIntegrationServiceImpl implements IntegrationService {

    private final InternalClient client;
    private final IntegrationConfig config;
    private final ContractorMapper mapper;

    private static final String SUFFIX = "/autoservice";

    @Override
    public UUID add(Contractor contractor, String password, String authorization, UUID oauthId) {
        var contractorDto = mapper.map(contractor, password, oauthId);
        var baseUrl = URI.create(config.getClientUrl(getType()) + SUFFIX);
        var response = client.add(baseUrl, contractorDto, authorization);
        return response.id();
    }

    @Override
    public ContractorType getType() {
        return ContractorType.AUTOSERVICE_INTERNAL;
    }

}
