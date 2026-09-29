package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeRequestDto;

import java.util.UUID;

/**
 * @author skakun-a Сервис для работы с видами траспортного средства
 */
@Validated
public interface TypeService {
    TypeDto get(UUID id);
    
    TypeDto create(@Valid TypeRequestDto requestDto);
    
    TypeDto update(UUID id, @Valid TypeRequestDto requestDto);
    
    void delete(UUID id);
    
    Page<TypeDto> findAll(PaginationCommonRequestDto paginationRequest);
}
