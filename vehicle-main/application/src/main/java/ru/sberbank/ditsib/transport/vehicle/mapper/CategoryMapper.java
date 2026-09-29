package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.Category;
import ru.sberbank.ditsib.transport.vehicle.dto.category.CategoryDto;

import java.util.List;

/**
 * @author skakun-a
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto categoryToCategoryDto(Category entity);
    
    Category categoryDtoToCategory(CategoryDto dto);
    
    List<CategoryDto> listCategoryToCategoryDto(List<Category> entities);
}
