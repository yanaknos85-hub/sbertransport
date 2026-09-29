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
import ru.sberbank.ditsib.transport.vehicle.controller.ModelController;
import ru.sberbank.ditsib.transport.vehicle.dto.model.ModelDto;
import ru.sberbank.ditsib.transport.vehicle.dto.model.ModelRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.ModelService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ModelControllerImpl implements ModelController {
    private final ModelService service;
    
    @Override
    public ModelDto getById(UUID modelId, Authentication authentication) {
        return service.get(modelId);
    }
    
    @Override
    public ModelDto add(ModelRequestDto requestDto, Authentication authentication) {
        return service.create(requestDto);
    }
    
    @Override
    public void update(UUID modelId, ModelRequestDto requestDto, Authentication authentication) {
        service.update(modelId, requestDto);
    }
    
    @Override
    public void delete(UUID modelId) {
        service.delete(modelId);
    }
    
    @Override
    public Page<ModelDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return service.findAll(paginationRequest);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dbConstraintsCheck(DataIntegrityViolationException exception) {
        log.error("Exception occurred ", exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Удаление невозможно в связи с наличием связных записей")).build();
    }
}
