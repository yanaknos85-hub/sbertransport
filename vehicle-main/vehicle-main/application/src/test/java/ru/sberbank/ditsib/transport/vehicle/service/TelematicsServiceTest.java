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
import ru.sberbank.ditsib.transport.vehicle.database.dao.TelematicsRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Telematics;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsDto;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.TelematicsMapper;
import ru.sberbank.ditsib.transport.vehicle.service.impl.TelematicsServiceImpl;

import java.util.List;
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
class TelematicsServiceTest {

    @Spy
    private final TelematicsMapper mapper = Mappers.getMapper(TelematicsMapper.class);

    @InjectMocks
    private TelematicsServiceImpl telematcisService;

    @Mock
    private TelematicsRepository telematicsRepository;

    @Test
    @DisplayName("Запрет добавления неуникального значения, совпадающего по imei")
    void duplicatedValuesShouldBeFailed() {
        var requestDto = new TelematicsRequestDto("12345678901111", "Сантел-навигация");
        telematcisService.create(requestDto);
        when(telematicsRepository.findByImeiOrTitleIgnoreCase(requestDto.imei(), requestDto.title().toUpperCase()))
                .thenReturn(List.of(Telematics.builder()
                        .id(UUID.randomUUID())
                        .imei(requestDto.imei())
                        .title(requestDto.title())
                        .build()));
        assertThrows(EntityAlreadyExistsException.class, () -> telematcisService.create(requestDto));
    }

    @Test
    @DisplayName("Обновление телметатики")
    void titleShouldBeUpdated() {
        var telematics = Instancio.create(Telematics.class);
        when(telematicsRepository.findById(telematics.getId())).thenReturn(Optional.of(telematics));

        var requestDto = new TelematicsRequestDto("12345678901111", "Сантел-навигация");
        var updatedBrand = telematcisService.update(telematics.getId(), requestDto);

        assertThat(updatedBrand)
                .isNotNull()
                .extracting(TelematicsDto::imei, TelematicsDto::title)
                .containsExactly("12345678901111", "Сантел-навигация");
    }

    @Test
    @DisplayName("Обновление телематики с неуникальным imei должен приводить к исключительной ситуации")
    void imeiShouldNotBeUpdated() {
        var telematics = new Telematics(UUID.randomUUID(), "12345678901111", "Навигация");
        var requestDto = new TelematicsRequestDto("12345678901111", "Сантел-навигация");

        when(telematicsRepository.findByImeiOrTitleIgnoreCase(telematics.getImei(), requestDto.title().toUpperCase()))
                .thenReturn(List.of(telematics));

        var telemeticsForUpdate = Instancio.create(Telematics.class);
        var telematicsForUpdateId = telemeticsForUpdate.getId();
        when(telematicsRepository.findById(telematicsForUpdateId)).thenReturn(Optional.of(telemeticsForUpdate));


        assertThrows(EntityAlreadyExistsException.class,
                () -> telematcisService.update(telematicsForUpdateId, requestDto));
    }

    @Test
    @DisplayName("Удаление должно происходить успешно")
    void deleteEntity() {
        var type = Instancio.create(Telematics.class);
        when(telematicsRepository.findById(type.getId())).thenReturn(Optional.of(type));

        telematcisService.delete(type.getId());

        verify(telematicsRepository, times(1)).delete(any());
    }

}
