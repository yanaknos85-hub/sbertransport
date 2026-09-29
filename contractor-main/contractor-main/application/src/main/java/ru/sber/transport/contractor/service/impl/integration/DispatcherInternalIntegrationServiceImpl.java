package ru.sber.transport.contractor.service.impl.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.contractor.config.IntegrationConfig;
import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.dto.PatchData;
import ru.sber.transport.contractor.feign.InternalClient;
import ru.sber.transport.contractor.mappers.ContractorMapper;
import ru.sber.transport.contractor.service.IntegrationService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DispatcherInternalIntegrationServiceImpl implements IntegrationService {

    private final InternalClient client;
    private final IntegrationConfig config;
    private final ContractorMapper mapper;
    private static final String MAIN_DISPATCHER_ID_FIELD = "mainDispatcherId";

    @Override
    public UUID add(Contractor contractor, String password, String authorization, UUID oauthId) {
        var contractorDto = mapper.map(contractor, password, oauthId);
        var baseUrl = URI.create(config.getClientUrl(getType()));
        var response = client.add(baseUrl, contractorDto, authorization);
        return response.id();
    }

    @Override
    public void delete(UUID id, String authorization) {
        var baseUrl = URI.create(config.getClientUrl(getType()));
        client.delete(baseUrl, id, authorization);
    }

    @Override
    public void changeContactPerson(Contractor contractor, String authorization, UUID dispatcherId) {
        var baseUrl = URI.create(config.getClientUrl(getType()));
        var data = new PatchData(MAIN_DISPATCHER_ID_FIELD, dispatcherId.toString());
        client.patchContractor(baseUrl, contractor.getExternalId(), List.of(data), authorization);
    }

    @Override
    public ContractorType getType() {
        return ContractorType.DISPATCHER_INTERNAL;
    }
}
