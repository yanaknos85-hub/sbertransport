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
import ru.sberbank.ditsib.transport.vehicle.database.dao.EngineTypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.EngineTypeMapper;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.EngineTypeMessage;
import ru.sberbank.ditsib.transport.vehicle.messaging.sender.EngineTypeSender;
import ru.sberbank.ditsib.transport.vehicle.service.impl.EngineTypeServiceImpl;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author skakun-a
 */
@ExtendWith(MockitoExtension.class)
class EngineTypeServiceTest {

    @Spy
    private final EngineTypeMapper mapper = Mappers.getMapper(EngineTypeMapper.class);

    @InjectMocks
    private EngineTypeServiceImpl engineTypeService;

    @Mock
    private EngineTypeRepository engineTypeRepository;

    @Mock
    private EngineTypeSender engineTypeSender;


    @Test
    @DisplayName("Запрет добавления неуникального значения, совпадающего по названию")
    void duplicatedValuesShouldBeFailed() {
        var requestDto = new EngineTypeRequestDto("БЕНЗИНк");
        engineTypeService.create(requestDto);
        when(engineTypeRepository.findByTitle(requestDto.title())).thenReturn(Optional.of(EngineType.builder()
                .id(UUID.randomUUID())
                .title(requestDto.title())
                .build()));
        assertThrows(EntityAlreadyExistsException.class, () -> engineTypeService.create(requestDto));
    }

    @Test
    @DisplayName("Наименование Типа двигателя ТС должно быть обновлено по ID")
    void titleShouldBeUpdated() {
        var engineType = Instancio.create(EngineType.class);
        when(engineTypeRepository.findById(engineType.getId())).thenReturn(Optional.of(engineType));

        doNothing().when(engineTypeSender).send(any(EngineTypeMessage.class));
        var requestDto = new EngineTypeRequestDto("ЭЛЕКТРИЧЕСКИЙ");
        var updatedEngineTypes = engineTypeService.update(engineType.getId(), requestDto);

        assertThat(updatedEngineTypes)
                .isNotNull()
                .extracting(EngineTypeDto::title)
                .isEqualTo("ЭЛЕКТРИЧЕСКИЙ");
    }

    @Test
    @DisplayName("Удаление должно происходить успешно")
    void deleteEntity() {
        var engineType = Instancio.create(EngineType.class);
        when(engineTypeRepository.findById(engineType.getId())).thenReturn(Optional.of(engineType));
        doNothing().when(engineTypeSender).send(any(EngineTypeMessage.class));
        engineTypeService.delete(engineType.getId());

        verify(engineTypeRepository, times(1)).delete(any());
    }


    @Test
    @DisplayName("Список типов двигателей ТС должен быть выдан согласно порядка выборки из БД")
    void findAll() {
        var engineTypes = Stream.of("БЕНЗИН",
                        "ДИЗЕЛЬ",
                        "ЭЛЕКТРИЧЕСКИЙ",
                        "ГИБРИДНЫЙ")
                .map(title -> EngineType.builder().title(title).build())
                .collect(Collectors.toList());

        when(engineTypeRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(engineTypes));

        var foundEngineTypes = engineTypeService.findAll(new PageSettingDto(0, 10));

        assertThat(foundEngineTypes)
                .hasSize(4)
                .extracting(EngineTypeDto::title)
                .containsAll(engineTypes.stream().map(EngineType::getTitle).collect(Collectors.toList()));
    }

    @Test
    @DisplayName("Запрос без паригации должен возвращать все элементы")
    void getAllWithoutPagination() {
        var engineTypes = Stream.of("БЕНЗИН",
                        "ДИЗЕЛЬ",
                        "ЭЛЕКТРИЧЕСКИЙ",
                        "ГИБРИДНЫЙ")
                .map(title -> EngineType.builder().title(title).build())
                .collect(Collectors.toList());

        when(engineTypeRepository.findAll(any(Sort.class))).thenReturn(engineTypes);

        var foundEngineTypes = engineTypeService.findAll(null);

        assertThat(foundEngineTypes)
                .hasSize(4)
                .extracting(EngineTypeDto::title)
                .containsAll(engineTypes.stream().map(EngineType::getTitle).collect(Collectors.toList()));
    }

}
