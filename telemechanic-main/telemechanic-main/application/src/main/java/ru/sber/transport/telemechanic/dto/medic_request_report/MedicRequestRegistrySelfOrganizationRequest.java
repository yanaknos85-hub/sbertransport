package ru.sber.transport.telemechanic.dto.medic_request_report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.enumerate.MedicRequestField;
import ru.sber.transport.telemechanic.enumerate.MedicRequestSortOption;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record MedicRequestRegistrySelfOrganizationRequest(
        
        @NotEmpty(message = "fieldSet должен быть заполнен")
        @Schema(description = "Набор полей для формирования реестра медицинских осмотров",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Set<MedicRequestField> fieldSet,
        
        @Size(min = 3, max = 16, message = "searchText должен быть не меньше 3 и не больше 16 символов")
        @Schema(description = "Поиск по номеру заявки",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "TL-0000-000000",
                nullable = true,
                minimum = "3",
                maximum = "16")
        String searchText,
        
        @Schema(description = "Табельный номер заявителя",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "1234567",
                nullable = true)
        String personnelNumber,
        
        @Size(min = 3, max = 16, message = "humanReadableId должен быть не меньше 3 и не больше 16 символов")
        @Schema(description = "Номер заявки",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "TL-0000-000000",
                nullable = true,
                minimum = "3",
                maximum = "16")
        String humanReadableId,
        
        @Schema(description = "Набор идентификаторов подразделений для фильтрации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        Set<UUID> departmentIdSet,
        
        @Valid
        @Schema(description = "Период создания",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        DateRange period,
        
        @Valid
        @Schema(description = "Настройки сортировки",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        SortSetting sortSetting,
        
        @Schema(description = "Настройки пагинации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true)
        PageSettingDto pageSetting
) {
    
    public PageRequest preparePageRequest() {
        return Objects.nonNull(pageSetting) ? PageRequest.of(pageSetting.page(), pageSetting.size(), getSort())
                                            : PageRequest.of(0, 10, getSort());
    }
    
    private Sort getSort() {
        if (Objects.isNull(sortSetting)) {
            return Sort.by(Sort.Direction.ASC, MedicRequestSortOption.MEDIC_REQUEST_HUMAN_READABLE_ID.getSqlValue());
        }
        var direction = sortSetting.directionAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
        return sortSetting.property.equals(MedicRequestSortOption.MEDIC_REQUEST_HUMAN_READABLE_ID)
               ? Sort.by(direction, MedicRequestSortOption.MEDIC_REQUEST_HUMAN_READABLE_ID.getSqlValue())
               : Sort.by(direction, MedicRequestSortOption.MEDIC_ORGANIZATION_NAME.getSqlValue());
    }
    
    @Schema(name = "MedicRequestRegistryForAllOrganizations.SortSetting",
            title = "Настройка сортировки",
            description = "Настройка сортировки")
    public record SortSetting(
            @NotNull(message = "Необходимо указать своство сортировки")
            @Schema(description = "Свойство сортировки",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "MEDIC_REQUEST_HUMAN_READABLE_ID")
            MedicRequestSortOption property,
            
            @NotNull(message = "Необходимо указать направление сортировки")
            @Schema(description = "Направление сортировки - по возрастанию",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "true")
            Boolean directionAsc
    ) {
    }
}
