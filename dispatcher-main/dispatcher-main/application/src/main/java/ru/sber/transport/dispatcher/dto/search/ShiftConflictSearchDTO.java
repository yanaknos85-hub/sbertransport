package ru.sber.transport.dispatcher.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

@Getter
@Setter
@Schema(title = "Фильтры для поиска конфликтов смен", description = "Параметры поиска конфликтов смен")
public class ShiftConflictSearchDTO extends PageSortFilterParameters<ShiftConflictParameters> {

    public ShiftConflictSearchDTO() {
        super(ShiftConflictParameters.CONFLICT_REASON);
    }

    @Schema(description = "Табельный номер водителя")
    private String personnelNumber;

    @Schema(description = "Государственный номер автомобиля")
    private String stateNumber;

    @Schema(description = "Причина конфликта")
    private String conflictReason;

}
