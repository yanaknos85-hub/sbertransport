package ru.sber.transport.telemechanic.dto.ewb.search;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.telemechanic.database.model.Ewb_;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Schema(name = "EwbSearchDto", description = "Запрос на поиск ЭПЛ")
public record EwbSearchDto(
        @Schema(description = "Текст для поиска", example = "TM-0001-00005589", minLength = 3, maxLength = 100)
        @Size(min = 3, max = 36)
        String searchText,
        @Schema(description = "Гос. номер авто", example = "A123AA777", maxLength = 50)
        @Size(max = 50)
        String stateNumber,
        @Schema(description = "Статусы ЭПЛ", example = "[ON_THE_LINE]")
        Set<EwbStatus> requestStatusSet,
        @Schema(description = "Временной интервал создания ЭПЛ")
        DateRange creationTime,
        @Schema(description = "Временной интервал завершения ЭПЛ")
        DateRange finishTime,
        @Schema(description = "Список идентификаторов контрагентов")
        List<UUID> contractorIds,
        @Schema(description = "Список идентификаторов филиалов контрагентов")
        List<UUID> autoparkIds,
        @Schema(description = "Настройки пагинации")
        PageSettingDto pageSetting
) {
    
    public PageRequest preparePageRequest() {
        return pageSetting == null ?
               PageRequest.of(0, 10, getSort()) :
               PageRequest.of(pageSetting.page(), pageSetting.size(), getSort());
    }
    
    public Sort getSort() {
        return Sort.by(Ewb_.STATUS).descending().and(Sort.by(Ewb_.START_DATE));
    }
    
    
}
