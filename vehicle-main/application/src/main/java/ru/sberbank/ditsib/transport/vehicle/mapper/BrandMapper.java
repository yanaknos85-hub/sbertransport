package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.Brand;
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandDto;

import java.util.List;

/**
 * @author skakun-a
 */
@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandDto brandToBrandDto(Brand entity);
    
    Brand brandDtoToBrand(BrandDto dto);
    
    List<BrandDto> listBrandToBrandDto(List<Brand> entities);
}
