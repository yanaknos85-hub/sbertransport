package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;
import ru.sberbank.ditsib.transport.reports.controller.UserAttributesController;
import ru.sberbank.ditsib.transport.reports.dto.*;
import ru.sberbank.ditsib.transport.reports.service.UserAttributesService;

import jakarta.validation.Valid;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@E2EController
public class UserAttributesControllerImpl implements UserAttributesController {
    
    private final UserAttributesService userAttributesService;
    
    @Override
    public UserAttributesDTO getUserAttributes(@E2EUser("principal") JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return userAttributesService.get(userId);
    }
    
    @Override
    public UserAttributesDTO editTaxiUserAttributes(@E2EUser("principal") JwtAuthenticationToken authentication, @Valid TaxiUIVisibilityDTO newData) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return userAttributesService.update(userId, newData);
    }
    
    @Override
    public UserAttributesDTO editPersonalUserAttributes(@E2EUser("principal") JwtAuthenticationToken authentication, @Valid PersonalUIVisibilityDTO newData) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return userAttributesService.update(userId, newData);
    }
    
    @Override
    public UserAttributesDTO editPublicUserAttributes(@E2EUser("principal") JwtAuthenticationToken authentication, @Valid PublicUIVisibilityDTO newData) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return userAttributesService.update(userId, newData);
    }
    
    @Override
    public UserAttributesDTO editCarsharingUserAttributes(@E2EUser("principal") JwtAuthenticationToken authentication, @Valid CarsharingUIVisibilityDTO newData) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return userAttributesService.update(userId, newData);
    }
    
    @Override
    public UserAttributesDTO getDefaultUserAttributes() {
        return userAttributesService.getDefaultAttributes();
    }
}
