package ru.sberbank.ditsib.transport.vehicle.service;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.sberbank.ditsib.transport.vehicle.database.dao.CategoryRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Category;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.category.CategoryDto;
import ru.sberbank.ditsib.transport.vehicle.dto.category.CategoryRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.CategoryMapper;
import ru.sberbank.ditsib.transport.vehicle.service.impl.CategoryServiceImpl;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author skakun-a
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    
    @Spy
    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);
    
    @InjectMocks
    private CategoryServiceImpl categoryService;
    
    @Mock
    private CategoryRepository brandRepository;
    
    @Test
    @DisplayName("Завпрет добавления неуникального значения, совпадающего по названию")
    void duplicatedValuesShouldBeFailed() {
        var requestDto = new CategoryRequestDto("А", "Мопеды, мотоциклы");
        categoryService.create(requestDto);
        when(brandRepository.findByCategoryOrTitle(requestDto.category(), requestDto.title()))
                .thenReturn(Optional.of(Category.builder()
                                                .id(UUID.randomUUID())
                                                .category(
                                                        requestDto.category())
                                                .title(requestDto.title())
                                                .build()));
        assertThrows(EntityAlreadyExistsException.class, () -> categoryService.create(requestDto));
    }
    
    @Test
    @DisplayName("Название Марки ТС должно быть обновлено по ID")
    void titleShouldBeUpdated() {
        var category = Instancio.create(Category.class);
        category.setCategory("C");
        when(brandRepository.findById(category.getId())).thenReturn(Optional.of(category));
        
        var requestDto = new CategoryRequestDto("A", "Мопеды, мотоциклы");
        var updatedBrand = categoryService.update(category.getId(), requestDto);
        
        assertThat(updatedBrand)
                .isNotNull()
                .extracting(CategoryDto::category, CategoryDto::title)
                .containsExactly("A", "Мопеды, мотоциклы");
    }
    
    @Test
    @DisplayName("Удаление должно происходить успешно")
    void deleteEntity() {
        var category = Instancio.create(Category.class);
        category.setCategory("A1");
        when(brandRepository.findById(category.getId())).thenReturn(Optional.of(category));
        
        categoryService.delete(category.getId());
        
        verify(brandRepository, times(1)).delete(any());
    }
    
    
    @Test
    @DisplayName("Список Моделей ТС должен быть выдан согласно порядка выборки из БД")
    void findAll() {
        var categories = IntStream.range(0, 7)
                                  .mapToObj(i -> Category.builder()
                                                         .category(String.format("A%d", i))
                                                         .title(Instancio.create(String.class))
                                                         .build())
                                  .collect(Collectors.toList());
        when(brandRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(categories));
        
        var foundCategories = categoryService.findAll(new PageSettingDto(0, 10));
        
        assertThat(foundCategories)
                .hasSize(7)
                .extracting(CategoryDto::title)
                .containsAll(categories.stream().map(Category::getTitle).collect(Collectors.toList()));
        assertThat(foundCategories)
                .extracting(CategoryDto::category)
                .containsAll(categories.stream().map(Category::getCategory).collect(Collectors.toList()));
    }
    
    @Test
    @DisplayName("Запрос без паригации должен возвращать все элементы")
    void getAllWithoutPagination() {
        var categories = IntStream.range(0, 23)
                                  .mapToObj(i -> Category.builder()
                                                         .category(String.format("A%d", i))
                                                         .title(Instancio.create(String.class))
                                                         .build())
                                  .collect(Collectors.toList());
        when(brandRepository.findAll(any(Sort.class))).thenReturn(categories);
        
        var foundCategories = categoryService.findAll(null);
        
        assertThat(foundCategories)
                .hasSize(23)
                .extracting(CategoryDto::title)
                .containsAll(categories.stream().map(Category::getTitle).collect(Collectors.toList()));
        assertThat(foundCategories)
                .extracting(CategoryDto::category)
                .containsAll(categories.stream().map(Category::getCategory).collect(Collectors.toList()));
    }
}
