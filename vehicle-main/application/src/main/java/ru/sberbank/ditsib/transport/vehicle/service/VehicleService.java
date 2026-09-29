package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PageVehicleWithFilters;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleSearchDto;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleShortDto;

import java.util.UUID;

/**
 * Сервис для работы с записями транспортных средств.
 */
@Validated
public interface VehicleService {

    void create(@Valid VehicleRequestDto requestDto);
    
    void delete(UUID id);
    
    Page<VehicleShortDto> findAll(PageSettingDto pageSetting);

    PageVehicleWithFilters search(VehicleSearchDto searchDto);
}
