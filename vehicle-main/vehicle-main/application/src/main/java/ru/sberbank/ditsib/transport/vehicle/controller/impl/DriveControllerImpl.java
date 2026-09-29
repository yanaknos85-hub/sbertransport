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
import ru.sberbank.ditsib.transport.vehicle.controller.DriveController;
import ru.sberbank.ditsib.transport.vehicle.dto.drive.DriveDto;
import ru.sberbank.ditsib.transport.vehicle.dto.drive.DriveRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.DriveService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DriveControllerImpl implements DriveController {
    
    private final DriveService driveService;
    
    @Override
    public DriveDto getById(UUID driveId, Authentication authentication) {
        return driveService.get(driveId);
    }
    
    @Override
    public DriveDto add(DriveRequestDto requestDto, Authentication authentication) {
        return driveService.create(requestDto);
    }
    
    @Override
    public void update(UUID driveId, DriveRequestDto requestDto, Authentication authentication) {
        driveService.update(driveId, requestDto);
    }
    
    @Override
    public void delete(UUID driveId) {
        driveService.delete(driveId);
    }
    
    @Override
    public Page<DriveDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return driveService.findAll(Optional.ofNullable(paginationRequest)
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
