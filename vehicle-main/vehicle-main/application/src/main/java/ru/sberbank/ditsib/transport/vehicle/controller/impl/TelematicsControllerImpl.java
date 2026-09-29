package ru.sberbank.ditsib.transport.vehicle.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.vehicle.controller.TelematicsController;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsDto;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.TelematicsService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TelematicsControllerImpl implements TelematicsController {

    private final TelematicsService service;

    @Override
    public TelematicsDto getById(UUID telematicsId, Authentication authentication) {
        return service.get(telematicsId);
    }

    @Override
    public TelematicsDto add(TelematicsRequestDto requestDto, Authentication authentication) {
        return service.create(requestDto);
    }

    @Override
    public void update(UUID telematicsId, TelematicsRequestDto requestDto, Authentication authentication) {
        service.update(telematicsId, requestDto);
    }

    @Override
    public void delete(UUID telematicsId) {
        service.delete(telematicsId);
    }

    @Override
    public Page<TelematicsDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return service.findAll(paginationRequest);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dbConstraintsCheck(DataIntegrityViolationException exception) {
        log.error("Exception occurred ", exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Удаление невозможно в связи с наличием связных записей")).build();
    }
}
