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
import ru.sberbank.ditsib.transport.vehicle.controller.FuelTypeController;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.FuelTypeService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FuelTypeControllerImpl implements FuelTypeController {
    private final FuelTypeService service;
    @Override
    public FuelTypeDto getById(UUID fuelTypeId, Authentication authentication) {
        return service.get(fuelTypeId);
    }
    
    @Override
    public void add(FuelTypeRequestDto requestDto, Authentication authentication) {
        service.create(requestDto);
    }
    
    @Override
    public void update(UUID fuelTypeId, FuelTypeRequestDto requestDto, Authentication authentication) {
        service.update(fuelTypeId, requestDto);
    }
    
    @Override
    public void delete(UUID fuelTypeId) {
        service.delete(fuelTypeId);
    }
    
    @Override
    public Page<FuelTypeDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return service.findAll(Optional.ofNullable(paginationRequest)
                                       .map(PaginationCommonRequestDto::pageSetting)
                                       .orElse(null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dbConstraintsCheck(DataIntegrityViolationException exception) {
        log.error("Exception occurred ", exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Удаление невозможно в связи с наличием связных записей")).build();
    }}
