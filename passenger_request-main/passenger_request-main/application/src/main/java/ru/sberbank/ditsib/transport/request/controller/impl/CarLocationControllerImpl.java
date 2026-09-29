package ru.sberbank.ditsib.transport.request.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.CarLocationController;
import ru.sberbank.ditsib.transport.request.service.CarLocationService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@E2EController
public class CarLocationControllerImpl implements CarLocationController {

    private final CarLocationService carLocationService;

    @Override
    public void getCarLocation(UUID requestId) {
        carLocationService.addRequestToTask(requestId);
    }
}
