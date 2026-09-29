package ru.sberbank.ditsib.transport.vehicle.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import ru.sberbank.ditsib.transport.vehicle.dto.category.CategoryDto;
import ru.sberbank.ditsib.transport.vehicle.dto.category.CategoryRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;

import java.util.UUID;

/**
 * @author skakun-a Сервис для работы с категорией траспортного средства
 */
@Validated
public interface CategoryService {
    CategoryDto get(UUID id);
    
    CategoryDto create(@Valid CategoryRequestDto requestDto);
    
    CategoryDto update(UUID id, @Valid CategoryRequestDto requestDto);
    
    void delete(UUID id);
    
    Page<CategoryDto> findAll(PageSettingDto pageSetting);
}
