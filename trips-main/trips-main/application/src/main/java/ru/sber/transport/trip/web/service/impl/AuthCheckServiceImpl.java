package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.business.model.Dispatcher;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.web.service.AuthCheckService;

import java.util.Collection;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthCheckServiceImpl implements AuthCheckService {

    private final DispatcherProvider dispatcherProvider;

    private final DriverProvider driverProvider;

    @Value("${dispatcher-room.admin.role:ROLE_DISPATCHER_ROOM_ADMIN}")
    private String dispatcherRoomAdminRole;

    @Value("${dispatcher-room.federal-dispatcher.role:ROLE_FEDERAL_DISPATCHER_CONTRACTOR}")
    private String federalDispatcherRole;


    @Override
    @SuppressWarnings("unchecked")
    public Dispatcher dispatcherAuthCheck(UUID contractorId, JwtAuthenticationToken authentication) {
        var roles = (Collection<? extends String>) authentication.getToken().getClaims().get("roles");
        if(roles.contains(dispatcherRoomAdminRole) || roles.contains(federalDispatcherRole)) {
            return null;
        }
        Dispatcher dispatcher;
        var userId = UUID.fromString(authentication.getToken().getId());
        if(contractorId != null) {
            dispatcher = dispatcherProvider.get(contractorId, userId)
                    .orElseGet(() -> dispatcherProvider.getByContractorIdAndOauthId(contractorId, userId).orElse(null));
        } else {
            dispatcher = dispatcherProvider.get(userId)
                    .orElseGet(() -> dispatcherProvider.getByOauthId(userId).orElse(null));
        }
        if (dispatcher == null) {
            throw new EntityNotFoundException(Dispatcher.class, userId);
        } else return dispatcher;
    }

    @Override
    @SuppressWarnings("unchecked")
    public UUID userAuthCheck(UUID contractorId, JwtAuthenticationToken authentication) {
        var roles = (Collection<? extends String>) authentication.getToken().getClaims().get("roles");
        if(roles.contains(dispatcherRoomAdminRole) || roles.contains(federalDispatcherRole)) {
            return contractorId;
        } else {
            var userId = UUID.fromString(authentication.getToken().getId());
            var dispatcher = dispatcherProvider.get(userId)
                    .orElseGet(() -> dispatcherProvider.getByOauthId(userId).orElse(null));
            if(dispatcher == null){
                return driverProvider.get(userId)
                        .orElseGet(() -> driverProvider.getByOauthId(userId)
                                .orElseThrow(() -> new EntityNotFoundException(Driver.class, userId))).getContractorId();
            } else return dispatcher.getContractorId();
        }
    }
}
