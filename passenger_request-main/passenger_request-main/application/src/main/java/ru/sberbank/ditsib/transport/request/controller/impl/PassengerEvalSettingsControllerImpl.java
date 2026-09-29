package ru.sberbank.ditsib.transport.request.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.PassengerEvalSettingsController;
import ru.sberbank.ditsib.transport.request.dto.PassengerEvalSettingsDTO;
import ru.sberbank.ditsib.transport.request.service.EvalSettingsService;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@E2EController
public class PassengerEvalSettingsControllerImpl implements PassengerEvalSettingsController {
    private final EvalSettingsService evalSettingsService;
    
    @Override
    public Collection<? extends PassengerEvalSettingsDTO> get() {
        return evalSettingsService.get();
    }
}