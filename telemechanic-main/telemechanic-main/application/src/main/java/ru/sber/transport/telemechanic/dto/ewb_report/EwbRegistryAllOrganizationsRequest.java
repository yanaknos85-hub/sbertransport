package ru.sber.transport.telemechanic.dto.ewb_report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.With;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.EwbSortOption;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.enumerate.EwbRegistryField;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static ru.sber.transport.telemechanic.enumerate.EwbRegistryField.EWB_HUMAN_READABLE_ID;

@With
@Schema(name = "EwbRegistryAllOrganizationsRequest", title = "Данные для фильтрации и пагинации реестра ЭПЛ по всем организациям",
        description = "Запрос с данными для фильтрации и пагинации реестра ЭПЛ по всем организациям")
public record EwbRegistryAllOrganizationsRequest(
        @Schema(description = "Набор дополнительных полей для формирования реестра ЭПЛ",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        Set<EwbRegistryField> fieldSet,
        
        @Size(min = 3, max = 20, message = "Размер ID путевого листа должен быть не меньше 3 символов и не больше 20 символов")
        @Schema(description = "Значение для поиска по ID путевого листа",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "001",
                minimum = "3", maximum = "20",
                nullable = true)
        String searchText,
        
        @Size(min = 3, max = 20, message = "Размер ID путевого листа должен быть не меньше 3 символов и не больше 20 символов")
        @Schema(description = "ID путевого листа",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "PL-0001-11111111",
                minimum = "3", maximum = "20",
                nullable = true)
        String humanReadableId,
        
        @Schema(description = "Идентификатор организации для фильтрации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                nullable = true)
        UUID organizationId,
        
        @Schema(description = "Набор идентификаторов подразделений для фильтрации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        Set<UUID> departmentIds,
        
        @Valid
        @Schema(description = "Период создания ЭПЛ",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        DateRange period,
        
        @Schema(description = "Настройки пагинации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        PageSettingDto pageSetting,
        
        @Schema(description = "Настройки сортировки",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        SortSetting sortSetting
) {
    public PageRequest preparePageRequest() {
        return Objects.nonNull(pageSetting) ? PageRequest.of(pageSetting().page(), pageSetting().size(), getSort())
                                            : PageRequest.of(0, 10, getSort());
    }
    
    private Sort getSort() {
        if (Objects.isNull(sortSetting)) {
            return Sort.by(Sort.Direction.ASC, EWB_HUMAN_READABLE_ID.getSqlViewFieldName());
        }
        var direction = sortSetting.directionAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
        return sortSetting.property().equals(EwbSortOption.ORGANIZATION_NAME) ? Sort.by(direction, EwbSortOption.ORGANIZATION_NAME.getSqlValue())
                                                                              : Sort.by(direction, EwbSortOption.HUMAN_READABLE_ID.getSqlValue());
    }
    
    @Schema(name = "EwbRegistrationAllOrganizationsRequest.SortSetting", title = "Настройки сортировки", description = "Настройки сортировки")
    public record SortSetting(
            @NotNull(message = "Необходимо указать свойство")
            @Schema(description = "Свойство сортировки",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "HUMAN_READABLE_ID")
            EwbSortOption property,
            
            @NotNull(message = "Необходимо указать направление сортировки")
            @Schema(description = "Направление сортировки - по возрастанию",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "true")
            Boolean directionAsc
    ) {
    }
}
