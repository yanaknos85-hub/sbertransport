package ru.sber.transport.telemechanic.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.telemechanic.database.model.Employee_;


@Getter
@RequiredArgsConstructor
@Schema(name = "DriverSortOption", title = "Варианты сортировки водителей", description = "Варианты сортировки водителей")
public enum DriverSortOption {
    
    PERSONNEL_NUMBER("Номер персонального удостоверения", Employee_.PERSONNEL_NUMBER),
    ORGANIZATION_NAME("Наименование организации", "o.official_name");
    
    private final String description;
    private final String fieldName;
}
