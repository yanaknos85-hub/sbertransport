package ru.sber.transport.telemechanic.dto.dispatcher;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.telemechanic.database.model.Dispatcher_;
import ru.sber.transport.telemechanic.database.model.Employee_;
import ru.sber.transport.telemechanic.database.model.Organization_;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.enumerate.DispatcherSortOption;

import java.util.UUID;

@Schema(name = "SearchDispatcherRequest", title = "Данные запроса для поиска диспетчеров")
public record SearchDispatcherRequest(
        @Schema(description = "Идентификатор организации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        UUID organizationId,
        @Schema(description = "Идентификатор подразделения",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        UUID departmentId,
        @Schema(description = "Флаг активности",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean active,
        @Schema(description = "Табельный номер",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "123456789")
        String personnelNumber,
        @Schema(description = "Настройки сортировки",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        SortSetting sortSetting,
        @Schema(description = "Настройка пагинации",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        PageSettingDto pageSetting
) {
    public PageRequest preparePageRequest() {
        return pageSetting != null ? PageRequest.of(pageSetting.page(), pageSetting.size(), getSort())
                                   : PageRequest.of(0, 10, getSort());
    }
    
    private Sort getSort() {
        if (sortSetting == null) {
            return Sort.by(Sort.Direction.ASC, Dispatcher_.EMPLOYEE + "." + Employee_.PERSONNEL_NUMBER);
        }
        var direction = sortSetting.directionAsc == null ? Sort.Direction.ASC
                                                         : sortSetting.directionAsc()
                                                           ? Sort.Direction.ASC
                                                           : Sort.Direction.DESC;
        return switch (sortSetting.property()) {
            case PERSONNEL_NUMBER -> Sort.by(direction, Dispatcher_.EMPLOYEE + "." + Employee_.PERSONNEL_NUMBER);
            case ORGANIZATION_NAME -> Sort.by(direction, Dispatcher_.ORGANIZATION + "." + Organization_.OFFICIAL_NAME);
        };
    }
    
    public record SortSetting(
            DispatcherSortOption property,
            Boolean directionAsc
    ) {
    }
}
