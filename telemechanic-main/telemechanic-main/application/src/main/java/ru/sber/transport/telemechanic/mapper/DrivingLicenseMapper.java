package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.telemechanic.database.model.Category;
import ru.sber.transport.telemechanic.database.model.DrivingLicense;
import ru.sber.transport.telemechanic.dto.driver.DrivingLicenseInfo;
import ru.sber.transport.telemechanic.dto.driver.GetDriverResponse;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DrivingLicenseMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "series", source = "series")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "issueDate", source = "issueDate")
    @Mapping(target = "expiryDate", source = "expiryDate")
    @Mapping(target = "previousId", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "categories", expression = "java(categoryIdsToCategories(source.categoryIds()))")
    DrivingLicense drivingLicenseInfoToDrivingLicense(DrivingLicenseInfo source);
    
    @Mapping(target = "categoryIds", expression = "java(categoriesToCategoryIds(source.getCategories()))")
    GetDriverResponse.DrivingLicenseDto drivingLicenseToDrivingLicenseDto(DrivingLicense source);
    
    default Set<Category> categoryIdsToCategories(Set<UUID> categoryIds) {
        return categoryIds.stream()
                .map(el -> new Category().setId(el))
                .collect(Collectors.toSet());
    }
    
    default Set<UUID> categoriesToCategoryIds(Set<Category> categories) {
        return categories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
    }
}
