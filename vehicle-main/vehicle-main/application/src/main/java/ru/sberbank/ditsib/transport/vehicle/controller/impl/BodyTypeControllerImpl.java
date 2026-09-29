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
import ru.sberbank.ditsib.transport.vehicle.controller.BodyTypeController;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.bodytype.BodyTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.bodytype.BodyTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.BodyTypeService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BodyTypeControllerImpl implements BodyTypeController {
    private final BodyTypeService bodyTypeService;
    @Override
    public BodyTypeDto getById(UUID bodyTypeId, Authentication authentication) {
        return bodyTypeService.get(bodyTypeId);
    }

    @Override
    public BodyTypeDto add(BodyTypeRequestDto requestDto, Authentication authentication) {
        return bodyTypeService.create(requestDto);
    }

    @Override
    public void update(UUID bodyTypeId, BodyTypeRequestDto requestDto, Authentication authentication) {
        bodyTypeService.update(bodyTypeId, requestDto);
    }

    @Override
    public void delete(UUID bodyTypeId) {
        bodyTypeService.delete(bodyTypeId);
    }

    @Override
    public Page<BodyTypeDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return bodyTypeService.findAll(paginationRequest);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dbConstraintsCheck(DataIntegrityViolationException exception) {
        log.error("Exception occurred ", exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Удаление невозможно в связи с наличием связных записей")).build();
    }
}
