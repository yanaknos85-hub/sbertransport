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
import ru.sberbank.ditsib.transport.vehicle.controller.TransmissionTypeController;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransmissionTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransmissionTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.TransmissionTypeService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TransmissionTypeControllerImpl implements TransmissionTypeController {

    private final TransmissionTypeService service;

    @Override
    public TransmissionTypeDto getById(UUID transmissionTypeId, Authentication authentication) {
        return service.get(transmissionTypeId);
    }

    @Override
    public TransmissionTypeDto add(TransmissionTypeRequestDto requestDto, Authentication authentication) {
        return service.create(requestDto);
    }

    @Override
    public void update(UUID transmissionTypeId, TransmissionTypeRequestDto requestDto, Authentication authentication) {
        service.update(transmissionTypeId, requestDto);
    }

    @Override
    public void delete(UUID transmissionTypeId) {
        service.delete(transmissionTypeId);
    }

    @Override
    public Page<TransmissionTypeDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return service.findAll(paginationRequest);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dbConstraintsCheck(DataIntegrityViolationException exception) {
        log.error("Exception occurred ", exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Удаление невозможно в связи с наличием связных записей")).build();
    }

}
