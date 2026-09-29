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
import ru.sberbank.ditsib.transport.vehicle.controller.CategoryController;
import ru.sberbank.ditsib.transport.vehicle.dto.category.CategoryDto;
import ru.sberbank.ditsib.transport.vehicle.dto.category.CategoryRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.service.CategoryService;

import java.util.Optional;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryControllerImpl implements CategoryController {
    
    private final CategoryService categoryService;
    
    @Override
    public CategoryDto getById(UUID categoryId, Authentication authentication) {
        return categoryService.get(categoryId);
    }
    
    @Override
    public CategoryDto add(CategoryRequestDto requestDto, Authentication authentication) {
        return categoryService.create(requestDto);
    }
    
    @Override
    public void update(UUID categoryId, CategoryRequestDto requestDto, Authentication authentication) {
        categoryService.update(categoryId, requestDto);
    }
    
    @Override
    public void delete(UUID categoryId) {
        categoryService.delete(categoryId);
    }
    
    @Override
    public Page<CategoryDto> getAll(PaginationCommonRequestDto paginationRequest) {
        return categoryService.findAll(Optional.ofNullable(paginationRequest)
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
