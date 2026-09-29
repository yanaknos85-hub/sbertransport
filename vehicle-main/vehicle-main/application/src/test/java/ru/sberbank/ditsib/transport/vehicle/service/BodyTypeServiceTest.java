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
import ru.sberbank.ditsib.transport.vehicle.database.dao.BodyTypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.BodyType;
import ru.sberbank.ditsib.transport.vehicle.dto.bodytype.BodyTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.bodytype.BodyTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.BodyTypeMapper;
import ru.sberbank.ditsib.transport.vehicle.service.impl.BodyTypeServiceImpl;

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
class BodyTypeServiceTest {

    @Spy
    private final BodyTypeMapper mapper = Mappers.getMapper(BodyTypeMapper.class);

    @InjectMocks
    private BodyTypeServiceImpl service;

    @Mock
    private BodyTypeRepository repository;

    @Test
    @DisplayName("Завпрет добавления неуникального значения, совпадающего по названию")
    void duplicatedValuesShouldBeFailed() {
        var requestDto = new BodyTypeRequestDto("Седан");
        service.create(requestDto);
        when(repository.findByTitle(requestDto.title()))
                .thenReturn(Optional.of(BodyType.builder()
                        .id(UUID.randomUUID())
                        .title(requestDto.title())
                        .build()));
        assertThrows(EntityAlreadyExistsException.class, () -> service.create(requestDto));
    }

    @Test
    @DisplayName("Обновление")
    void titleShouldBeUpdated() {
        var entity = Instancio.create(BodyType.class);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        var requestDto = new BodyTypeRequestDto("Седан ");
        var updatedResult = service.update(entity.getId(), requestDto);

        assertThat(updatedResult)
                .isNotNull()
                .extracting(BodyTypeDto::title)
                .isEqualTo("Седан");
    }

    @Test
    @DisplayName("Удаление должно происходить успешно")
    void deleteEntity() {
        var entity = Instancio.create(BodyType.class);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        service.delete(entity.getId());

        verify(repository, times(1)).delete(any());
    }

}
