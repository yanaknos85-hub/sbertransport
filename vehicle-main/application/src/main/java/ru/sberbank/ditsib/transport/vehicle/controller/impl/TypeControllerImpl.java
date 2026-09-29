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
import ru.sberbank.ditsib.transport.vehicle.controller.TypeController;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.TypeService;

import java.util.UUID;

/**
 * @author skakun-a
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class TypeControllerImpl implements TypeController {
    
    private final TypeService service;
    
    @Override
    public TypeDto getById(UUID typeId, Authentication authentication) {
        return service.get(typeId);
    }
    
    @Override
    public TypeDto add(TypeRequestDto requestDto, Authentication authentication) {
        return service.create(requestDto);
    }
    
    @Override
    public void update(UUID typeId, TypeRequestDto requestDto, Authentication authentication) {
        service.update(typeId, requestDto);
    }
    
    @Override
    public void delete(UUID typeId) {
        service.delete(typeId);
    }
    
    @Override
    public Page<TypeDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return service.findAll(paginationRequest);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dbConstraintsCheck(DataIntegrityViolationException exception) {
        log.error("Exception occurred ", exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Удаление невозможно в связи с наличием связных записей")).build();
    }
}
