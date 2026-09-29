package ru.sber.transport.authentication.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sber.transport.authentication.web.model.RegistrationRequestDto;
import ru.sber.transport.authentication.web.model.RegistrationResponseDto;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;

@RequestMapping("/registration")
@Tag(name = "Регистрация", description = "Набор операций для регистрации в системе")
public interface RegistrationController {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @NoAuthorize
    @Operation(summary = "Регистрация",
            description = "Регистрация учётной записи")
    RegistrationResponseDto register(@Valid @RequestBody RegistrationRequestDto dto);

}
