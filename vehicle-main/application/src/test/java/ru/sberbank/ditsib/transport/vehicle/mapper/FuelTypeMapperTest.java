package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelType;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeDto;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Тест маппера типов топлива")
class FuelTypeMapperTest {

    private final FuelTypeMapper mapper = Mappers.getMapper(FuelTypeMapper.class);

    @Test
    void fuelTypeToFuelTypeDto() {
        var source = Instancio.create(FuelType.class);
        var expected = new FuelTypeDto(source.getId(),
                source.getTitle(),
                mapper.fuelTypeNameListToStringList(source.getFuelTypeNames()),
                new EngineTypeDto(source.getEngineType().getId(), source.getEngineType().getTitle()));

        var actual = mapper.fueltTypeToFuelTypeDto(source);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void listFuelTypeToListFuelTypeDto() {
        var source = Instancio.createList(FuelType.class);
        var expected = source.stream().map(mapper::fueltTypeToFuelTypeDto).toList();
        var actual = mapper.listFuelTypeToListFuelTypeDto(source);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testFuelTypeToFuelTypeMessage() {
        var source = Instancio.create(FuelType.class);
        var deleted = true;

        var result = mapper.fuelTypeToFuelTypeMessage(source, deleted);

        assertThat(result.id()).isEqualTo(source.getId());
        assertThat(result.engineTypeId()).isEqualTo(source.getEngineType().getId());
        assertThat(result.title()).isEqualTo(source.getTitle());
        assertThat(result.possibleTitles()).isEqualTo(mapper.fuelTypeNameListToStringList(source.getFuelTypeNames()));
        assertThat(result.deleted()).isTrue();
    }

    @Test
    void fuelTypeToFuelTypeUUIDs() {
        var fuelTypes = Instancio.ofSet(FuelType.class).create();
        var result = mapper.fuelTypesToFuelTypeUUIDs(fuelTypes);

        assertEquals(result, fuelTypes.stream().map(FuelType::getId).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @MethodSource("fuelTypeProvider")
    void fuelTypeToFuelTypeUUID(FuelType fuelType) {
        var result = mapper.fuelTypeToFuelTypeUUID(fuelType);

        if (fuelType == null) {
            assertNull(result);
        } else assertEquals(result, fuelType.getId());
    }

    static Stream<FuelType> fuelTypeProvider() {
        return Stream.of(Instancio.create(FuelType.class), null);
    }
}
