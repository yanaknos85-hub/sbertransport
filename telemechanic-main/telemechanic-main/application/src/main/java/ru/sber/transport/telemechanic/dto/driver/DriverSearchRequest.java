package ru.sber.transport.telemechanic.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.telemechanic.dto.PageSettingDto;

import java.util.Objects;
import java.util.UUID;

import static ru.sber.transport.telemechanic.dto.driver.DriverSortOption.PERSONNEL_NUMBER;


@Schema(name = "DriverSearchRequestDto", title = "Запрос поиска водителей",
        description = "Запрос поиска водителей с параметрами поиска, пагинацией и сортировкой")
public record DriverSearchRequest(
        @Schema(description = "Идентификатор организации для фильтрации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "5e8bf20f-6e01-40b2-8f3a-2091d9c79820",
                nullable = true)
        UUID organizationId,
        
        @Schema(description = "Идентификатор подразделения для фильтрации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "ad8caee9-fbc1-4e85-9573-d3baf49140e4",
                nullable = true)
        UUID departmentId,
        
        @Schema(description = "Флаг активности водителя для фильтрации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "true",
                nullable = true)
        Boolean active,
        
        @Schema(description = "Табельный номер водителя для фильтрации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "234",
                nullable = true)
        String personnelNumber,
        
        @Schema(description = "Настройки пагинации",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "{\"page\": 0, \"size\": 10}")
        PageSettingDto pageSetting,
        
        @Schema(description = "Настройки сортировки",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "{\"property\": \"PERSONNEL_NUMBER\", \"directionAsc\": \"true\"}")
        SortSettingDto sortSetting) {
    
    public PageRequest preparePageRequest() {
        return pageSetting == null ? PageRequest.of(0, 10, getSort())
                                   : PageRequest.of(pageSetting().page(), pageSetting().size(), getSort());
    }
    
    private Sort getSort() {
        if (Objects.isNull(sortSetting)) {
            return Sort.by(Sort.Direction.ASC, PERSONNEL_NUMBER.getFieldName());
        }
        var direction = sortSetting.directionAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, sortSetting.property().getFieldName());
    }
    
    
    @Schema(name = "DriverSearchRequestDto.SortSetting", title = "Настройки сортировки", description = "Настройки сортировки")
    public record SortSettingDto(
            @NotNull(message = "Необходимо выбрать поле для сортировки")
            @Schema(description = "Поле для сортировки", requiredMode = Schema.RequiredMode.REQUIRED, example = "PERSONNEL_NUMBER")
            DriverSortOption property,
            
            @NotNull(message = "Необходимо выбрать направление сортировки")
            @Schema(description = "Направление сортировки по возрастанию", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
            Boolean directionAsc) {
    }
}
