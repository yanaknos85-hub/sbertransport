package ru.sber.transport.dispatcher.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.request.PageSortFilterParameters;
import ru.sberbank.ditsib.validation.interval.Interval;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(title = "Поиск контрагента", description = "Фильтры для контрагента")
@Interval(startField = "ratingFrom", endField = "ratingTo", inclusion = Interval.Include.INCLUDE)
public class ContractorSearchDTO extends PageSortFilterParameters<ContractorSearchParameters> {

    /**
     * Создать новый объект.
     */
    public ContractorSearchDTO() {
        super(ContractorSearchParameters.NAME);
    }

    @Schema(description = "Идентификатор контрагента")
    private UUID id;

    @Schema(description = "Название контрагента")
    private String name;

    @Pattern(regexp = "\\d{10}|\\d{12}", message = "ИНН должен состоять из 10 или 12 чисел")
    @Schema(description = "ИНН, 10 или 12 знаков ")
    private String tin;

    @Pattern(regexp = "\\d{13}", message = "ОРГН должен состоять из 13 чисел")
    @Schema(description = "ОРГН, 13 знаков")
    private String msrn;

    @Schema(description = "Рейтинг контрагента, минимальное и максимальное значения")
    @Min(0)
    private Integer ratingFrom;

    @Max(500)
    private Integer ratingTo;

    @Schema(description = "Признак внутреннего автопарка")
    private Boolean isInternal;
}
