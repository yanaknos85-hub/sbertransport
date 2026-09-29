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
import ru.sberbank.ditsib.transport.vehicle.database.dao.BrandRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Brand;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandDto;
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.BrandMapper;
import ru.sberbank.ditsib.transport.vehicle.service.impl.BrandServiceImpl;

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
class BrandServiceTest {
    
    @Spy
    private final BrandMapper mapper = Mappers.getMapper(BrandMapper.class);
    
    @InjectMocks
    private BrandServiceImpl brandService;
    
    @Mock
    private BrandRepository brandRepository;
    
    @Test
    @DisplayName("Завпрет добавления неуникального значения, совпадающего по названию")
    void duplicatedValuesShouldBeFailed() {
        var requestDto = new BrandRequestDto("Lada");
        brandService.create(requestDto);
        when(brandRepository.findByTitle(requestDto.title())).thenReturn(Optional.of(Brand.builder()
                                                                                          .id(UUID.randomUUID())
                                                                                          .title(requestDto.title())
                                                                                          .build()));
        assertThrows(EntityAlreadyExistsException.class, () -> brandService.create(requestDto));
    }
    
    @Test
    @DisplayName("Название Марки ТС должно быть обновлено по ID")
    void titleShouldBeUpdated() {
        var brand = Instancio.create(Brand.class);
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));
        
        var requestDto = new BrandRequestDto("Lada");
        var updatedBrand = brandService.update(brand.getId(), requestDto);
        
        assertThat(updatedBrand)
                .isNotNull()
                .extracting(BrandDto::title)
                .isEqualTo("Lada");
    }
    
    @Test
    @DisplayName("Удаление должно происходить успешно")
    void deleteEntity() {
        var brand = Instancio.create(Brand.class);
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));
        
        brandService.delete(brand.getId());
        
        verify(brandRepository, times(1)).delete(any());
    }
    
    
    @Test
    @DisplayName("Список Моделей ТС должен быть выдан согласно порядка выборки из БД")
    void findAll() {
        var brands = IntStream.range(0, 10)
                              .mapToObj(i -> Brand.builder().build())
                              .collect(Collectors.toList());
        when(brandRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(brands));
        
        var foundBrands = brandService.findAll(new PaginationCommonRequestDto(new PageSettingDto(0, 10), null));
        
        assertThat(foundBrands)
                .hasSize(10)
                .extracting(BrandDto::title)
                .containsAll(brands.stream().map(Brand::getTitle).collect(Collectors.toList()));
    }
    
}
