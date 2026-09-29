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
import ru.sberbank.ditsib.transport.vehicle.database.dao.DriveRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Drive;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.drive.DriveDto;
import ru.sberbank.ditsib.transport.vehicle.dto.drive.DriveRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.DriveMapper;
import ru.sberbank.ditsib.transport.vehicle.service.impl.DriveServiceImpl;

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
class DriveServiceTest {
    
    @Spy
    private final DriveMapper mapper = Mappers.getMapper(DriveMapper.class);
    
    @InjectMocks
    private DriveServiceImpl driveService;
    
    @Mock
    private DriveRepository driveRepository;
    
    @Test
    @DisplayName("Запрет добавления неуникального значения, совпадающего по названию")
    void duplicatedValuesShouldBeFailed() {
        var requestDto = new DriveRequestDto("Передний");
        driveService.create(requestDto);
        when(driveRepository.findByTitle(requestDto.title())).thenReturn(Optional.of(Drive.builder()
                                                                                          .id(UUID.randomUUID())
                                                                                          .title(requestDto.title())
                                                                                          .build()));
        assertThrows(EntityAlreadyExistsException.class, () -> driveService.create(requestDto));
    }
    
    @Test
    @DisplayName("Наименование привода ТС должно быть обновлено по ID")
    void titleShouldBeUpdated() {
        var drive = Instancio.create(Drive.class);
        when(driveRepository.findById(drive.getId())).thenReturn(Optional.of(drive));
        
        var requestDto = new DriveRequestDto("передний");
        var updatedEntineTypes = driveService.update(drive.getId(), requestDto);
        
        assertThat(updatedEntineTypes)
                .isNotNull()
                .extracting(DriveDto::title)
                .isEqualTo("передний");
    }
    
    @Test
    @DisplayName("Удаление должно происходить успешно")
    void deleteEntity() {
        var drive = Instancio.create(Drive.class);
        when(driveRepository.findById(drive.getId())).thenReturn(Optional.of(drive));
        
        driveService.delete(drive.getId());
        
        verify(driveRepository, times(1)).delete(any());
    }
    
    
    @Test
    @DisplayName("Список приводов ТС должен быть выдан согласно порядка выборки из БД")
    void findAll() {
        var drives = Stream.of("передний",
                                    "задний",
                                    "полный")
                              .map(title -> Drive.builder().title(title).build())
                              .collect(Collectors.toList());
        
        when(driveRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(drives));
        
        var foundEngineTypes = driveService.findAll(new PageSettingDto(0, 10));
        
        assertThat(foundEngineTypes)
                .hasSize(3)
                .extracting(DriveDto::title)
                .containsAll(drives.stream().map(Drive::getTitle).collect(Collectors.toList()));
    }
    
    @Test
    @DisplayName("Запрос без паригации должен возвращать все элементы")
    void getAllWithoutPagination() {
        var drives = Stream.of("передний",
                               "задний",
                               "полный")
                           .map(title -> Drive.builder().title(title).build())
                           .collect(Collectors.toList());
        
        when(driveRepository.findAll(any(Sort.class))).thenReturn(drives);
        
        var foundEngineTypes = driveService.findAll(null);
        
        assertThat(foundEngineTypes)
                .hasSize(3)
                .extracting(DriveDto::title)
                .containsAll(drives.stream().map(Drive::getTitle).collect(Collectors.toList()));
    }
}
