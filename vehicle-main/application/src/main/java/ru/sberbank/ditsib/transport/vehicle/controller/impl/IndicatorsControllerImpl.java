package ru.sberbank.ditsib.transport.vehicle.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.controller.IndicatorsController;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.GetIndicatorValueDto;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.IndicatorDateInfo;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.Indicators;
import ru.sberbank.ditsib.transport.vehicle.exception.IndicatorsException;
import ru.sberbank.ditsib.transport.vehicle.exception.NotAllowedUserException;
import ru.sberbank.ditsib.transport.vehicle.exception.StatusException;
import ru.sberbank.ditsib.transport.vehicle.exception.UserNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.helper.UserAuthorizationHelper;
import ru.sberbank.ditsib.transport.vehicle.service.TransportService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class IndicatorsControllerImpl implements IndicatorsController {
    
    private final TransportService transportService;
    
    @Override
    public void updateIndicators(UUID transportId, Indicators indicator, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        transportService.updateIndicators(transportId, indicator, userId);
    }
    
    @Override
    public IndicatorDateInfo getIndicatorsDateInfo(UUID transportId, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return transportService.getIndicatorsDateInfo(transportId, userId);
    }
    
    @Override
    public GetIndicatorValueDto getIndicatorValue(UUID transportId) {
        return transportService.getIndicatorValue(transportId);
    }
    
    @ExceptionHandler({ EntityNotFoundException.class, UserNotFoundException.class })
    public ResponseEntity<Object> handleException(EntityNotFoundException exception) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage())).build();
    }
    
    @ExceptionHandler({ StatusException.class,
                        NotAllowedUserException.class,
                        IndicatorsException.class })
    public ResponseEntity<Object> handleException(Exception exception) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage())).build();
    }
}
