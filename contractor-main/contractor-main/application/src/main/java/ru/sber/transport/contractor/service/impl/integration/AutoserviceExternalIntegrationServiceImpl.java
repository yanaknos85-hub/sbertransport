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
public class AutoserviceExternalIntegrationServiceImpl implements IntegrationService {

    private final InternalClient client;
    private final IntegrationConfig config;
    private final ContractorMapper mapper;

    private static final String SUFFIX = "/autoservice";

    @Override
    public UUID add(Contractor contractor, String password, String authorization, UUID oauthId) {
        var linkDTO = mapper.mapToLink(contractor, password);
        var baseUrl = URI.create(config.getClientUrl(getType()) + SUFFIX);
        client.linkContractor(baseUrl, linkDTO);
        return null;
    }

    @Override
    public ContractorType getType() {
        return ContractorType.AUTOSERVICE_EXTERNAL;
    }

}
