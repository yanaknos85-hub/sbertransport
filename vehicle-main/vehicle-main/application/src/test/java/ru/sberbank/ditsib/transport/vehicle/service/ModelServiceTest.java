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
import ru.sberbank.ditsib.transport.vehicle.database.dao.ModelRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Brand;
import ru.sberbank.ditsib.transport.vehicle.database.model.Model;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.model.ModelDto;
import ru.sberbank.ditsib.transport.vehicle.dto.model.ModelRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.ModelMapper;
import ru.sberbank.ditsib.transport.vehicle.service.impl.ModelServiceImpl;

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
class ModelServiceTest {
    
    @Spy
    private final ModelMapper mapper = Mappers.getMapper(ModelMapper.class);
    
    @InjectMocks
    private ModelServiceImpl modelService;
    
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private ModelRepository modelRepository;
    
    @Test
    @DisplayName("Завпрет добавления неуникального значения, совпадающего по названию")
    void duplicatedValuesShouldBeFailed() {
        var brand = Instancio.create(Brand.class);
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));
        
        var requestDto = new ModelRequestDto("CST-300", brand.getId());
        modelService.create(requestDto);
        when(modelRepository.findByTitle(requestDto.title())).thenReturn(Optional.of(Model.builder()
                                                                                          .id(UUID.randomUUID())
                                                                                          .title(requestDto.title())
                                                                                          .brand(Instancio.create(Brand.class))
                                                                                          .build()));
        assertThrows(EntityAlreadyExistsException.class, () -> modelService.create(requestDto));
    }
    
    @Test
    @DisplayName("Обновление наименования Модели ТС")
    void titleShouldBeUpdated() {
        var model = Instancio.create(Model.class);
        var brand = Instancio.create(Brand.class);
        model.setBrand(brand);
        
        when(modelRepository.findById(model.getId())).thenReturn(Optional.of(model));
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));
        
        var requestDto = new ModelRequestDto("CTS-300 ", brand.getId());
        var updateModel = modelService.update(model.getId(), requestDto);
        
        assertThat(updateModel)
                .isNotNull()
                .extracting(ModelDto::title)
                .isEqualTo("CTS-300");
    }
    @Test
    @DisplayName("Обновление Марки модели ТС")
    void typeShouldBeUpdated() {
        var model = Instancio.create(Model.class);
        var brand = Instancio.create(Brand.class);
        model.setBrand(brand);
        
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));
        
        when(modelRepository.findById(model.getId())).thenReturn(Optional.of(model));
        
        var requestDto = new ModelRequestDto("CTS-300 ", brand.getId());
        var updateModel = modelService.update(model.getId(), requestDto);
        
        assertThat(updateModel)
                .isNotNull()
                .extracting(ModelDto::title, t -> t.brand().title())
                .containsExactly("CTS-300", brand.getTitle());
    }
    
    @Test
    @DisplayName("Удаление должно происходить успешно")
    void deleteEntity() {
        var model = Instancio.create(Model.class);
        when(modelRepository.findById(model.getId())).thenReturn(Optional.of(model));
        
        modelService.delete(model.getId());
        
        verify(modelRepository, times(1)).delete(any());
    }
    
    
    @Test
    @DisplayName("Список Моделей ТС должен быть выдан согласно порядка выборки из БД")
    void findAll() {
        var brand = Instancio.create(Brand.class);
        var subtypes = IntStream.range(0, 10)
                             .mapToObj(i -> Model.builder().brand(brand).title(Integer.toString(i)).build())
                             .collect(Collectors.toList());
        when(modelRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(subtypes));
        
        var foundBrands = modelService.findAll(new PaginationCommonRequestDto(new PageSettingDto(0, 10), null));
        
        assertThat(foundBrands)
                .hasSize(10)
                .extracting(ModelDto::title)
                .containsAll(subtypes.stream().map(Model::getTitle).collect(Collectors.toList()));
    }
    
}
