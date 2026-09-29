package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.telemechanic.database.model.Region;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Тест маппера региона")
class RegionMapperTest {
    
    private final RegionMapper regionMapper = Mappers.getMapper(RegionMapper.class);
    
    @Test
    void mapRegionToDto() {
        var regionEntity = Instancio.create(Region.class);
        var regionDto = regionMapper.mapRegionToDto(regionEntity);
        assertThat(regionDto).isNotNull();
        assertThat(regionDto.title()).isEqualTo(regionEntity.getName() + " (" + regionEntity.getCode() + ")");
        assertThat(regionDto.code()).isEqualTo(regionEntity.getCode());
    }
}
