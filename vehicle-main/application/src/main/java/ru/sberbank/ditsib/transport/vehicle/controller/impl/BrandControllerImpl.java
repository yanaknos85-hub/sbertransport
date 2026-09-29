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
import ru.sberbank.ditsib.transport.vehicle.controller.BrandController;
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandDto;
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.BrandService;

import java.util.UUID;

/**
 * @author skakun-a
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class BrandControllerImpl implements BrandController {
    
    private final BrandService brandService;
    
    @Override
    public BrandDto getById(UUID id, Authentication authentication) {
        return brandService.get(id);
    }
    
    @Override
    public BrandDto add(BrandRequestDto requestDto, Authentication authentication) {
        return brandService.create(requestDto);
    }
    
    @Override
    public void update(UUID brandId, BrandRequestDto requestDto, Authentication authentication) {
        brandService.update(brandId, requestDto);
    }
    
    @Override
    public void delete(UUID brandId) {
        brandService.delete(brandId);
    }
    
    @Override
    public Page<BrandDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return brandService.findAll(paginationRequest);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dbConstraintsCheck(DataIntegrityViolationException exception) {
        log.error("Exception occurred ", exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Удаление невозможно в связи с наличием связных записей")).build();
    }
}
