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
import ru.sberbank.ditsib.transport.vehicle.database.dao.WheelSizeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.WheelSize;
import ru.sberbank.ditsib.transport.vehicle.dto.wheelsize.WheelSizeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.wheelsize.WheelSizeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.WheelSizeMapper;
import ru.sberbank.ditsib.transport.vehicle.service.impl.WheelSizeServiceImpl;

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
class WheelSizeServiceTest {

    @Spy
    private final WheelSizeMapper mapper = Mappers.getMapper(WheelSizeMapper.class);

    @InjectMocks
    private WheelSizeServiceImpl service;

    @Mock
    private WheelSizeRepository repository;

    @Test
    @DisplayName("Запрет добавления неуникального значения, совпадающего по названию")
    void duplicatedValuesShouldBeFailed() {
        var requestDto = new WheelSizeRequestDto("185/55R15 81V");
        service.create(requestDto);
        when(repository.findByTitle(requestDto.title()))
                .thenReturn(Optional.of(WheelSize.builder()
                        .id(UUID.randomUUID())
                        .title(requestDto.title())
                        .build()));
        assertThrows(EntityAlreadyExistsException.class, () -> service.create(requestDto));
    }

    @Test
    @DisplayName("Обновление")
    void titleShouldBeUpdated() {
        var entity = Instancio.create(WheelSize.class);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        var requestDto = new WheelSizeRequestDto("185/55R15 81V ");
        var updatedResult = service.update(entity.getId(), requestDto);

        assertThat(updatedResult)
                .isNotNull()
                .extracting(WheelSizeDto::title)
                .isEqualTo("185/55R15 81V");
    }

    @Test
    @DisplayName("Удаление должно происходить успешно")
    void deleteEntity() {
        var entity = Instancio.create(WheelSize.class);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        service.delete(entity.getId());

        verify(repository, times(1)).delete(any());
    }

}
