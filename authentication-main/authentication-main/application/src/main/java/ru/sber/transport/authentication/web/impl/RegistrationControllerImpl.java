package ru.sber.transport.authentication.web.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authentication.business.use_cases.AccountCases;
import ru.sber.transport.authentication.web.RegistrationController;
import ru.sber.transport.authentication.web.model.RegistrationRequestDto;
import ru.sber.transport.authentication.web.model.RegistrationResponseDto;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RegistrationControllerImpl implements RegistrationController {

    private final AccountCases accountCases;

    @Override
    public RegistrationResponseDto register(RegistrationRequestDto dto) {
        return accountCases.register(dto);
    }
}
