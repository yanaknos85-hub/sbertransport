package ru.sberbank.ditsib.transport.request.controller.carsharing.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.carsharing.CarsharingInfoController;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingInfoRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingInfoResponseDTO;
import ru.sberbank.ditsib.transport.request.service.CarsharingInfoService;

@RestController
@RequiredArgsConstructor
@E2EController
public class CarsharingInfoControllerImpl implements CarsharingInfoController {
    
    private final CarsharingInfoService carsharingInfoService;
    
    @Override
    public CarsharingInfoResponseDTO get() {
        return carsharingInfoService.get();
    }
    
    @Override
    public CarsharingInfoResponseDTO update(CarsharingInfoRequestDTO dto) {
        return carsharingInfoService.update(dto);
    }
    
}
