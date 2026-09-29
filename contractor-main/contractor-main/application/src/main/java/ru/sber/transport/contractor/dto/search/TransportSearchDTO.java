package ru.sber.transport.contractor.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.contractor.dto.enums.ResponseFields;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Schema(title = "Поиск свободного транспорта", description = "Фильтры для поиска свободного транспорта")
public class TransportSearchDTO {

    @Schema(description = "Дата начала периода")
    private OffsetDateTime startDate;

    @Schema(description = "Дата окончания периода")
    private OffsetDateTime endDate;

    @Schema(description = "Строка для полнотекстового поиска")
    private String search;

    @Schema(description = "Список полей для получения результата")
    private List<ResponseFields> result;

    @Schema(description = "Номер страницы")
    private Integer page;

    @Schema(description = "Размер страницы")
    private Integer size;

    @Schema(description = "Поле для сортировки")
    private String field;

    @Schema(description = "Направоление сортировки")
    private String direction;
}
