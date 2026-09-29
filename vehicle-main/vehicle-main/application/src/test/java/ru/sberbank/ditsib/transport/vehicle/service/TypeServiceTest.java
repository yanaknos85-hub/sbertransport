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
import ru.sberbank.ditsib.transport.vehicle.database.dao.TypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Type;
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.TypeMapper;
import ru.sberbank.ditsib.transport.vehicle.service.impl.TypeServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author skakun-a
 */
@ExtendWith(MockitoExtension.class)
class TypeServiceTest {
    
    @Spy
    private final TypeMapper mapper = Mappers.getMapper(TypeMapper.class);
    
    @InjectMocks
    
    private TypeServiceImpl typeService;
    
    @Mock
    private TypeRepository typeRepository;
    
    @Test
    @DisplayName("Завпрет добавления неуникального значения, совпадающего по названию")
    void duplicatedValuesShouldBeFailed() {
        var requestDto = new TypeRequestDto("Личный");
        typeService.create(requestDto);
        when(typeRepository.findByTitle(requestDto.title())).thenReturn(Optional.of(Type.builder()
                                                                                        .id(UUID.randomUUID())
                                                                                        .title(requestDto.title())
                                                                                        .build()));
        assertThrows(EntityAlreadyExistsException.class, () -> typeService.create(requestDto));
    }
    
    @Test
    @DisplayName("Обновление вида ТС")
    void titleShouldBeUpdated() {
        var type = Instancio.create(Type.class);
        when(typeRepository.findById(type.getId())).thenReturn(Optional.of(type));
        
        var requestDto = new TypeRequestDto("Служебный ");
        var updatedBrand = typeService.update(type.getId(), requestDto);
        
        assertThat(updatedBrand)
                .isNotNull()
                .extracting(TypeDto::title)
                .isEqualTo("Служебный");
    }
    
    @Test
    @DisplayName("Удаление должно происходить успешно")
    void deleteEntity() {
        var type = Instancio.create(Type.class);
        when(typeRepository.findById(type.getId())).thenReturn(Optional.of(type));
        
        typeService.delete(type.getId());
        
        verify(typeRepository, times(1)).delete(any());
    }
    
}
