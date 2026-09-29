package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandDto;
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;

import java.util.UUID;

/**
 * @author skakun-a Сервис для работы с моделью траспортного средства
 */
@Validated
public interface BrandService {
    BrandDto get(UUID id);
    
    BrandDto create(@Valid BrandRequestDto requestDto);
    
    BrandDto update(UUID id, @Valid BrandRequestDto requestDto);
    
    void delete(UUID id);
    
    Page<BrandDto> findAll(PaginationCommonRequestDto paginationCommonRequestDto);
}
