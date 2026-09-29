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
import ru.sberbank.ditsib.transport.vehicle.controller.EngineTypeController;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.EngineTypeService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EngineTypeControllerImpl implements EngineTypeController {
    private final EngineTypeService service;
    
    @Override
    public EngineTypeDto getById(UUID typeId, Authentication authentication) {
        return service.get(typeId);
    }
    
    @Override
    public EngineTypeDto add(EngineTypeRequestDto requestDto, Authentication authentication) {
        return service.create(requestDto);
    }
    
    @Override
    public void update(UUID typeId, EngineTypeRequestDto requestDto, Authentication authentication) {
        service.update(typeId, requestDto);
    }
    
    @Override
    public void delete(UUID typeId) {
        service.delete(typeId);
    }
    
    @Override
    public Page<EngineTypeDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return service.findAll(Optional.ofNullable(paginationRequest)
                                       .map(PaginationCommonRequestDto::pageSetting)
                                       .orElse(null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dbConstraintsCheck(DataIntegrityViolationException exception) {
        log.error("Exception occurred ", exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Удаление невозможно в связи с наличием связных записей")).build();
    }
}
