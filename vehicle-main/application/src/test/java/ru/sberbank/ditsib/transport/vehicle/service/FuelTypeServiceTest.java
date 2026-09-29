package ru.sberbank.ditsib.transport.vehicle.service;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import ru.sberbank.ditsib.transport.vehicle.database.dao.EngineTypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.FuelTypeNameRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.FuelTypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelType;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelTypeName;
import ru.sberbank.ditsib.transport.vehicle.database.model.OdometerHistory;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.mapper.FuelTypeMapper;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.FuelTypeMessage;
import ru.sberbank.ditsib.transport.vehicle.messaging.sender.FuelTypeSender;
import ru.sberbank.ditsib.transport.vehicle.service.impl.FuelTypeServiceImpl;
import ru.sberbank.ditsib.transport.vehicle.service.validation.FuelTypeValidationService;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author skakun-a
 */
@ExtendWith(MockitoExtension.class)
class FuelTypeServiceTest {

    @Spy
    private final FuelTypeMapper mapper = Mappers.getMapper(FuelTypeMapper.class);

    @InjectMocks
    private FuelTypeServiceImpl fuelTypeService;

    @Mock
    private EngineTypeRepository engineTypeRepository;
    @Mock
    private FuelTypeNameRepository fuelTypeNameRepository;
    @Mock
    private FuelTypeRepository fuelTypeRepository;
    @Mock
    private FuelTypeSender fuelTypeSender;
    @Mock
    private FuelTypeValidationService validationService;

    @Captor
    private ArgumentCaptor<List<FuelTypeName>> fuelTypeNameArgumentCaptor;

    @Test
    @DisplayName("Добавление вида топлива ТС")
    void createFuelType() {
        var engineType = Instancio.create(EngineType.class);
        var fuelType = Instancio.create(FuelType.class);
        var requestDto = Instancio.create(FuelTypeRequestDto.class);
        var title = requestDto.title().trim();
        doReturn(Optional.of(engineType)).when(engineTypeRepository).findById(requestDto.engineTypeId());
        doNothing().when(validationService).checkIfFuelTypeAlreadyExists(title, engineType);
        doReturn(fuelType).when(fuelTypeRepository).save(FuelType.builder()
                .title(title)
                .engineType(engineType)
                .build());
        doAnswer(invocationOnMock -> invocationOnMock.<OdometerHistory>getArgument(0))
                .when(fuelTypeNameRepository).saveAll(fuelTypeNameArgumentCaptor.capture());

        fuelTypeService.create(requestDto);

        verify(engineTypeRepository).findById(requestDto.engineTypeId());
        verify(validationService).checkIfFuelTypeAlreadyExists(title, engineType);
        verify(fuelTypeNameRepository).saveAll(fuelTypeNameArgumentCaptor.getValue());
        verify(mapper).fuelTypeToFuelTypeMessage(fuelType, false);
        verify(fuelTypeSender).send(any(FuelTypeMessage.class));

        var expectedFuelTypeNameList = requestDto.possibleTitles().stream()
                .filter(StringUtils::hasLength)
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .map(name -> FuelTypeName.builder()
                        .fuelTypeId(fuelType.getId())
                        .name(name)
                        .build())
                .toList();
        assertThat(fuelTypeNameArgumentCaptor.getValue()).isEqualTo(expectedFuelTypeNameList);
    }

    @Test
    @DisplayName("Обновление наименования Вида топлива ТС")
    void titleShouldBeUpdated() {
        var fuelType = Instancio.create(FuelType.class);
        var engineType = Instancio.create(EngineType.class);
        fuelType.setEngineType(engineType);

        doReturn(Optional.of(fuelType)).when(fuelTypeRepository).findById(fuelType.getId());
        doReturn(Optional.of(engineType)).when(engineTypeRepository).findById(engineType.getId());
        doNothing().when(fuelTypeSender).send(any(FuelTypeMessage.class));

        var requestDto = new FuelTypeRequestDto("АИ-95 ", engineType.getId(), List.of("АИ-95", "Топливо 95"));
        var updateModel = fuelTypeService.update(fuelType.getId(), requestDto);

        assertThat(updateModel)
                .isNotNull()
                .extracting(FuelTypeDto::title)
                .isEqualTo("АИ-95");
    }

    @Test
    @DisplayName("Обновление Типа двигателя ТС")
    void typeShouldBeUpdated() {
        var fuelType = Instancio.create(FuelType.class);
        var engineType = Instancio.create(EngineType.class);
        fuelType.setEngineType(engineType);

        doReturn(Optional.of(engineType)).when(engineTypeRepository).findById(engineType.getId());
        doReturn(Optional.of(fuelType)).when(fuelTypeRepository).findById(fuelType.getId());
        doNothing().when(fuelTypeSender).send(any(FuelTypeMessage.class));

        var requestDto = new FuelTypeRequestDto("АИ-95 ", engineType.getId(), List.of("АИ-95", "Топливо 95"));
        var updateModel = fuelTypeService.update(fuelType.getId(), requestDto);

        assertThat(updateModel)
                .isNotNull()
                .extracting(FuelTypeDto::title, ft -> ft.engineType().title())
                .containsExactly("АИ-95", engineType.getTitle());
    }

    @Test
    @DisplayName("Удаление должно происходить успешно")
    void deleteEntity() {
        var fuelType = Instancio.create(FuelType.class);
        doReturn(Optional.of(fuelType)).when(fuelTypeRepository).findById(fuelType.getId());
        doNothing().when(fuelTypeSender).send(any(FuelTypeMessage.class));
        fuelTypeService.delete(fuelType.getId());

        verify(fuelTypeRepository).delete(fuelType);
    }


    @Test
    @DisplayName("Список Вида топлива ТС должен быть выдан согласно порядка выборки из БД")
    void findAll() {
        var engineType = Instancio.create(EngineType.class);
        var fuelTypeList = IntStream.range(0, 10)
                .mapToObj(i -> FuelType.builder()
                        .engineType(engineType)
                        .title(Integer.toString(i))
                        .build())
                .toList();
        doReturn(new PageImpl<>(fuelTypeList))
                .when(fuelTypeRepository).findAllWithNamesAndEngineTypes(any(Pageable.class));

        var foundBrands = fuelTypeService.findAll(new PageSettingDto(0, 10));

        assertThat(foundBrands)
                .hasSize(10)
                .extracting(FuelTypeDto::title)
                .containsAll(fuelTypeList.stream()
                        .map(FuelType::getTitle)
                        .toList());
    }

    @Test
    @DisplayName("Запрос без пагинации должен возвращать все элементы")
    void getAllWithoutPagination() {
        var engineType = Instancio.create(EngineType.class);
        var fuelTypeList = IntStream.range(0, 25)
                .mapToObj(i -> FuelType.builder()
                        .engineType(engineType)
                        .title(Integer.toString(i))
                        .build())
                .toList();
        doReturn(fuelTypeList).when(fuelTypeRepository).findAllWithNamesAndEngineTypes(any(Sort.class));

        var foundBrands = fuelTypeService.findAll(null);

        assertThat(foundBrands)
                .hasSize(25)
                .extracting(FuelTypeDto::title)
                .containsAll(fuelTypeList.stream()
                        .map(FuelType::getTitle)
                        .toList());
    }
}
