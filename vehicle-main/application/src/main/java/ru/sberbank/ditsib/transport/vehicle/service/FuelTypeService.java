package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;

import java.util.UUID;

/**
 * @author skakun-a Сервис для работы с типами топлива траспортного средства
 */
@Validated
public interface FuelTypeService {
    FuelTypeDto get(UUID id);
    
    void create(@Valid FuelTypeRequestDto requestDto);
    
    FuelTypeDto update(UUID id, @Valid FuelTypeRequestDto requestDto);
    
    void delete(UUID id);
    
    Page<FuelTypeDto> findAll(PageSettingDto pageSetting);
}
