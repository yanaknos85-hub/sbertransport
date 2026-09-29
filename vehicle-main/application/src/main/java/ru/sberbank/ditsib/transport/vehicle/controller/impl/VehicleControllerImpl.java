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
import ru.sberbank.ditsib.transport.vehicle.controller.VehicleController;
import ru.sberbank.ditsib.transport.vehicle.dto.PageVehicleWithFilters;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleSearchDto;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleShortDto;
import ru.sberbank.ditsib.transport.vehicle.service.VehicleService;

import java.util.Optional;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@Slf4j
public class VehicleControllerImpl implements VehicleController {
    private final VehicleService service;
    
    @Override
    public void add(VehicleRequestDto requestDto, Authentication authentication) {
        service.create(requestDto);
    }
    
    @Override
    public void delete(UUID vehicleId) {
        service.delete(vehicleId);
    }
    
    @Override
    public Page<VehicleShortDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return service.findAll(Optional.ofNullable(paginationRequest)
                                            .map(PaginationCommonRequestDto::pageSetting)
                                            .orElse(null));
    }
    
    @Override
    public PageVehicleWithFilters search(VehicleSearchDto searchDto) {
        return service.search(searchDto);
    }
}
