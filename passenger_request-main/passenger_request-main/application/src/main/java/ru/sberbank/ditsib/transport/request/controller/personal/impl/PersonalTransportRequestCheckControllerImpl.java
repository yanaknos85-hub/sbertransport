package ru.sberbank.ditsib.transport.request.controller.personal.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.personal.PersonalTransportRequestCheckController;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalTransportSplitCheckRqDTO;
import ru.sberbank.ditsib.transport.request.service.personal.PersonalTransportRequestSplitCheckService;

@RequiredArgsConstructor
@RestController
@E2EController
public class PersonalTransportRequestCheckControllerImpl implements PersonalTransportRequestCheckController {
    private final PersonalTransportRequestSplitCheckService personalTransportRequestSplitCheckService;

    @Override
    public void splitCheck(PersonalTransportSplitCheckRqDTO request, JwtAuthenticationToken authentication) {
        personalTransportRequestSplitCheckService.check(request);
    }
}
