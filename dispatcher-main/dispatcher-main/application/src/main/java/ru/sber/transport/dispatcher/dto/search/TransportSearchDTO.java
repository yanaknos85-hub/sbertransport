package ru.sber.transport.dispatcher.dto.search;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.dispatcher.dto.enums.ResponseFields;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Schema(title = "Поиск свободного транспорта", description = "Фильтры для поиска свободного транспорта")
public class TransportSearchDTO extends PageSortFilterParameters<TransportSearchParameters> {

    public TransportSearchDTO() {
        super(TransportSearchParameters.BRAND);
    }

    @Schema(description = "Дата начала периода")
    private OffsetDateTime startDate;

    @Schema(description = "Дата окончания периода")
    private OffsetDateTime endDate;

    @Schema(description = "Временная зона")
    private String timeZone;

    @Schema(description = "Строка с данными для поиска")
    private String search;

    @Schema(description = "Список полей для получения результата")
    private List<ResponseFields> result;
}
