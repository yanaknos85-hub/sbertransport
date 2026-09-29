package ru.sber.transport.request_checks.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.request_checks.controller.MultipointRequestCheckController;
import ru.sber.transport.request_checks.dto.MultipointCheckRequestDto;
import ru.sber.transport.request_checks.service.TripRequestService;

@RestController
@RequiredArgsConstructor
@Validated
public class MultipointRequestCheckControllerImpl implements MultipointRequestCheckController {

    private final TripRequestService tripRequestService;

    @Override
    public void checkMultipointLimit(MultipointCheckRequestDto request, JwtAuthenticationToken authentication) {
        tripRequestService.checkMultipointLimit(request);
    }

}
