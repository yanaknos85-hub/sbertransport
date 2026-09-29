package ru.sber.transport.request_checks.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.request_checks.controller.DurationRequestCheckController;
import ru.sber.transport.request_checks.dto.DurationCheckRequestDto;
import ru.sber.transport.request_checks.service.TripRequestService;

@RestController
@RequiredArgsConstructor
@Validated
public class DurationRequestCheckControllerImpl implements DurationRequestCheckController {

    private final TripRequestService tripRequestService;

    @Override
    public void checkDurationLimit(DurationCheckRequestDto request, JwtAuthenticationToken authentication) {
        tripRequestService.checkDurationLimit(request);
    }

}
