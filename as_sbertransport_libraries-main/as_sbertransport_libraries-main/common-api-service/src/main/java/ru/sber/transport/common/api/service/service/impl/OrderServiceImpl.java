package ru.sber.transport.common.api.service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.common.api.service.config.JsonApiProperties;
import ru.sber.transport.common.api.service.model.dto.*;
import ru.sber.transport.common.api.service.aop.AutoAuthorization;
import ru.sber.transport.common.api.service.model.Credentials;
import ru.sber.transport.common.api.service.model.dto.cargo.CargoOrderRequest;
import ru.sber.transport.common.api.service.service.OrderService;
import ru.sber.transport.common.api.service.service.ValidationResponseService;
import ru.sber.transport.common.api.service.service.client.OrderClient;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ValidationResponseService validationService;
    private final OrderClient orderApi;
    private final Credentials credentials;
    private final JsonApiProperties jsonApiProperties;

    @Override
    @AutoAuthorization
    public OrderResponse create(CredentialClient auth, OrderRequest request) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = orderApi.createOrder(getRequestUri(auth), contractor.getTokenWithPrefix(), auth.getUri().toString(), request);
        validationService.validate(response);
        return response.getBody();
    }

    @Override
    @AutoAuthorization
    public List<OrderResponse> create(CredentialClient auth, List<CargoOrderRequest> request) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = orderApi.createOrder(getRequestUri(auth), contractor.getTokenWithPrefix(), auth.getUri().toString(), request);
        validationService.validate(response);
        return response.getBody();
    }

    @Override
    @AutoAuthorization
    public OrderInfoResponse info(CredentialClient auth, String orderParthnerID) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = orderApi.orderInfo(getRequestUri(auth), contractor.getTokenWithPrefix(), auth.getUri().toString(), orderParthnerID);
        validationService.validate(response);
        return response.getBody();
    }

    @Override
    @AutoAuthorization
    public CancelOrderResponse cancel(CredentialClient auth, String orderPartnerID) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = orderApi.cancel(
                getRequestUri(auth),
                contractor.getTokenWithPrefix(),
                auth.getUri().toString(),
                orderPartnerID,
                orderPartnerID
        );
        validationService.validate(response);
        return response.getBody();
    }

    @Override
    @AutoAuthorization
    public LocationResponse location(CredentialClient auth, String orderPartnerID) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = orderApi.location(getRequestUri(auth), contractor.getTokenWithPrefix(), auth.getUri().toString(), orderPartnerID);
        validationService.validate(response);
        return response.getBody();
    }

    @Override
    @AutoAuthorization
    public ShortOrderInfoResponse getAll(CredentialClient auth, OrderFilterRequest filterRequest) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = orderApi.ordersInfo(getRequestUri(auth), contractor.getTokenWithPrefix(), auth.getUri().toString(), filterRequest);
        validationService.validate(response);
        return response.getBody();
    }

    @Override
    @AutoAuthorization
    public OrderResponse updateOrder(CredentialClient auth, String orderPartnerID, UpdateOrderRequest updateOrderRequest) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = orderApi
                .updateOrder(getRequestUri(auth), contractor.getTokenWithPrefix(), auth.getUri().toString(), orderPartnerID, updateOrderRequest);
        validationService.validate(response);
        return response.getBody();
    }

    @Override
    @AutoAuthorization
    public RouteResponse routeOrder(CredentialClient auth, String orderPartnerID) {
        var contractor = credentials.get(auth.getUri(), auth.getLogin());
        var response = orderApi
                .route(getRequestUri(auth), contractor.getTokenWithPrefix(), auth.getUri().toString(), orderPartnerID);
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
