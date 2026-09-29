package ru.sber.transport.request_checks.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.request_checks.controller.OverrunRequestCheckController;
import ru.sber.transport.request_checks.dto.OverrunCheckRequestDto;
import ru.sber.transport.request_checks.dto.OverrunCheckResponseDto;
import ru.sber.transport.request_checks.service.TripRequestService;

@RestController
@RequiredArgsConstructor
@Validated
public class OverrunRequestCheckControllerImpl implements OverrunRequestCheckController {

    private final TripRequestService tripRequestService;

    @Override
    public OverrunCheckResponseDto checkOverrunLimit(OverrunCheckRequestDto request, JwtAuthenticationToken authentication) {
        return tripRequestService.checkOverrunLimit(request);
    }
}
