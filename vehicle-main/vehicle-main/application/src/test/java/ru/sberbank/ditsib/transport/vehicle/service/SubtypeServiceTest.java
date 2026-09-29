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
import ru.sberbank.ditsib.transport.vehicle.database.dao.SubtypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.TypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Subtype;
import ru.sberbank.ditsib.transport.vehicle.database.model.Type;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.subtype.SubtypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.subtype.SubtypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.SubtypeMapper;
import ru.sberbank.ditsib.transport.vehicle.service.impl.SubtypeServiceImpl;

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
class SubtypeServiceTest {
    
    @Spy
    private final SubtypeMapper mapper = Mappers.getMapper(SubtypeMapper.class);
    
    @InjectMocks
    private SubtypeServiceImpl subtypeService;
    
    @Mock
    private TypeRepository typeRepository;
    @Mock
    private SubtypeRepository subtypeRepository;
    
    @Test
    @DisplayName("Завпрет добавления неуникального значения, совпадающего по названию")
    void duplicatedValuesShouldBeFailed() {
        var type = Instancio.create(Type.class);
        when(typeRepository.findById(type.getId())).thenReturn(Optional.of(type));
        
        var requestDto = new SubtypeRequestDto("Судно", type.getId());
        subtypeService.create(requestDto);
        when(subtypeRepository.findByTitle(requestDto.title())).thenReturn(Optional.of(Subtype.builder()
                                                                                              .id(UUID.randomUUID())
                                                                                              .title(requestDto.title())
                                                                                              .type(Instancio.create(Type.class))
                                                                                              .build()));
        assertThrows(EntityAlreadyExistsException.class, () -> subtypeService.create(requestDto));
    }
    
    @Test
    @DisplayName("Обновление наименования подвида ТС")
    void titleShouldBeUpdated() {
        var subtype = Instancio.create(Subtype.class);
        var type = Instancio.create(Type.class);
        subtype.setType(type);
        
        when(subtypeRepository.findById(subtype.getId())).thenReturn(Optional.of(subtype));
        when(typeRepository.findById(type.getId())).thenReturn(Optional.of(type));
        
        var requestDto = new SubtypeRequestDto("СТС ", type.getId());
        var updateSubtype = subtypeService.update(subtype.getId(), requestDto);
        
        assertThat(updateSubtype)
                .isNotNull()
                .extracting(SubtypeDto::title)
                .isEqualTo("СТС");
    }
    @Test
    @DisplayName("Обновление вида подвида ТС")
    void typeShouldBeUpdated() {
        var subtype = Instancio.create(Subtype.class);
        var type = Instancio.create(Type.class);
        subtype.setType(type);
        
        when(typeRepository.findById(type.getId())).thenReturn(Optional.of(type));
        
        when(subtypeRepository.findById(subtype.getId())).thenReturn(Optional.of(subtype));
        
        var requestDto = new SubtypeRequestDto("Судно ", type.getId());
        var updateSubtype = subtypeService.update(subtype.getId(), requestDto);
        
        assertThat(updateSubtype)
                .isNotNull()
                .extracting(SubtypeDto::title, t -> t.type().title())
                .containsExactly("Судно", type.getTitle());
    }
    
    @Test
    @DisplayName("Удаление должно происходить успешно")
    void deleteEntity() {
        var subtype = Instancio.create(Subtype.class);
        when(subtypeRepository.findById(subtype.getId())).thenReturn(Optional.of(subtype));
        
        subtypeService.delete(subtype.getId());
        
        verify(subtypeRepository, times(1)).delete(any());
    }
    
    
    @Test
    @DisplayName("Список Подвидов ТС должен быть выдан согласно порядка выборки из БД")
    void findAll() {
        var type = Instancio.create(Type.class);
        var subtypes = IntStream.range(0, 10)
                             .mapToObj(i -> Subtype.builder().type(type).title(Integer.toString(i)).build())
                             .collect(Collectors.toList());
        when(subtypeRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(subtypes));
        
        var foundBrands = subtypeService.findAll(new PaginationCommonRequestDto(new PageSettingDto(0, 10), null));
        
        assertThat(foundBrands)
                .hasSize(10)
                .extracting(SubtypeDto::title)
                .containsAll(subtypes.stream().map(Subtype::getTitle).collect(Collectors.toList()));
    }
    
}
