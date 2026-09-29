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
import ru.sberbank.ditsib.transport.vehicle.controller.SubtypeController;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.subtype.SubtypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.subtype.SubtypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.SubtypeService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SubtypeControllerImpl implements SubtypeController {
    private final SubtypeService service;
    @Override
    public SubtypeDto getById(UUID subtypeId, Authentication authentication) {
        return service.get(subtypeId);
    }
    
    @Override
    public SubtypeDto add(SubtypeRequestDto requestDto, Authentication authentication) {
        return service.create(requestDto);
    }
    
    @Override
    public void update(UUID subtypeId, SubtypeRequestDto requestDto, Authentication authentication) {
        service.update(subtypeId, requestDto);
    }
    
    @Override
    public void delete(UUID subtypeId) {
        service.delete(subtypeId);
    }
    
    @Override
    public Page<SubtypeDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return service.findAll(paginationRequest);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dbConstraintsCheck(DataIntegrityViolationException exception) {
        log.error("Exception occurred ", exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Удаление невозможно в связи с наличием связных записей")).build();
    }
}
