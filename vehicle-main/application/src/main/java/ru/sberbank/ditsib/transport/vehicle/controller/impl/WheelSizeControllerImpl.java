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
import ru.sberbank.ditsib.transport.vehicle.controller.WheelSizeController;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.wheelsize.WheelSizeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.wheelsize.WheelSizeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.WheelSizeService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WheelSizeControllerImpl implements WheelSizeController {

    private final WheelSizeService service;

    @Override
    public WheelSizeDto getById(UUID wheelSizeId, Authentication authentication) {
        return service.get(wheelSizeId);
    }

    @Override
    public WheelSizeDto add(WheelSizeRequestDto requestDto, Authentication authentication) {
        return service.create(requestDto);
    }

    @Override
    public void update(UUID wheelSizeId, WheelSizeRequestDto requestDto, Authentication authentication) {
        service.update(wheelSizeId, requestDto);
    }

    @Override
    public void delete(UUID wheelSizeId) {
        service.delete(wheelSizeId);
    }

    @Override
    public Page<WheelSizeDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return service.findAll(paginationRequest);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dbConstraintsCheck(DataIntegrityViolationException exception) {
        log.error("Exception occurred ", exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Удаление невозможно в связи с наличием связных записей")).build();
    }

}
