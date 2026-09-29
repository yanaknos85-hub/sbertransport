package ru.sber.transport.common.api.service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.common.api.service.config.JsonApiProperties;
import ru.sber.transport.common.api.service.model.dto.*;
import ru.sber.transport.common.api.service.aop.AutoAuthorization;
import ru.sber.transport.common.api.service.model.Credentials;
import ru.sber.transport.common.api.service.service.ClientService;
import ru.sber.transport.common.api.service.service.ValidationResponseService;
import ru.sber.transport.common.api.service.service.client.ClientClient;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ValidationResponseService validationService;
    private final ClientClient clientApi;
    private final Credentials credentials;
    private final JsonApiProperties jsonApiProperties;


    @Override
    @AutoAuthorization
    public StatusResponse health(CredentialClient auth) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = clientApi.health(getRequestUri(auth), contractor.getTokenWithPrefix(), auth.getUri().toString());
        validationService.validate(response);
        return response.getBody();
    }

    @Override
    @AutoAuthorization
    public ClientInfoResponse clientInfo(CredentialClient auth) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = clientApi.clientInfo(auth.getUri(), contractor.getTokenWithPrefix());
        validationService.validate(response);
        return response.getBody();
    }

    @Override
    @AutoAuthorization
    public TariffsResponse getTariffs(CredentialClient auth, TariffFilterRequest filterRequest) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = clientApi.getTariffs(getRequestUri(auth), contractor.getTokenWithPrefix(), auth.getUri().toString(), filterRequest);
        validationService.validate(response);
        return response.getBody();
    }

    @Override
    @AutoAuthorization
    public CitiesResponse getCities(CredentialClient auth) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = clientApi.getCities(getRequestUri(auth), contractor.getTokenWithPrefix(), auth.getUri().toString());
        validationService.validate(response);
        return response.getBody();
    }


    private URI getRequestUri(CredentialClient credential) {
        if(jsonApiProperties.getRequestUrl() == null) {
            return credential.getUri();
        }

        return URI.create(jsonApiProperties.getRequestUrl());
    }
}
