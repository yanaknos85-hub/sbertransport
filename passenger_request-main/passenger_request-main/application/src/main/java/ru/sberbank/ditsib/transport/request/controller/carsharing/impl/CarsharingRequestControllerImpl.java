package ru.sberbank.ditsib.transport.request.controller.carsharing.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.carsharing.CarsharingRequestController;
import ru.sberbank.ditsib.transport.request.dto.GetRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.RequestCarsharingSearchDTO;
import ru.sberbank.ditsib.transport.request.service.RequestControllerService;

/**
 * Implementation of carsharing request controller service.
 */
@RequiredArgsConstructor
@RestController
@Slf4j
@E2EController
public class CarsharingRequestControllerImpl implements CarsharingRequestController {
    
    private final RequestControllerService requestControllerService;
    
    @Override
    public Page<? extends GetRequestDTO> getRequestsByCarsharingSearchDTO(RequestCarsharingSearchDTO requestSearchDTO) {
        return requestControllerService.carsharingSearch(requestSearchDTO);
    }
}
