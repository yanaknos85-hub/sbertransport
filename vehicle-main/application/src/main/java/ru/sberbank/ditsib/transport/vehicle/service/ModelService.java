package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import ru.sberbank.ditsib.transport.vehicle.dto.model.ModelDto;
import ru.sberbank.ditsib.transport.vehicle.dto.model.ModelRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;

import java.util.UUID;

/**
 * @author skakun-a Сервис для работы с моделями траспортного средства
 */
@Validated
public interface ModelService {
    ModelDto get(UUID id);
    
    ModelDto create(@Valid ModelRequestDto requestDto);
    
    ModelDto update(UUID id, @Valid ModelRequestDto requestDto);
    
    void delete(UUID id);
    
    Page<ModelDto> findAll(PaginationCommonRequestDto paginationRequest);
}
