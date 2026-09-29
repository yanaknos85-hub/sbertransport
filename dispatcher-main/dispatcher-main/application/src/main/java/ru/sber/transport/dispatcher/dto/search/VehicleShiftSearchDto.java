package ru.sber.transport.dispatcher.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.sberbank.ditsib.request.PageSortFilterParameters;
import ru.sberbank.ditsib.validation.interval.Interval;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Interval(startField = "startDate", endField = "endDate", inclusion = Interval.Include.INCLUDE)
public class VehicleShiftSearchDto extends PageSortFilterParameters<VehicleShiftSearchParameters> {

    public VehicleShiftSearchDto() {super(VehicleShiftSearchParameters.BRAND);}

    @Schema(description = "Дата начала смены")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @Schema(description = "Дата окончания смены")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    @Schema(description = "Поле для поиска по гос. номеру или марке автомобиля")
    private String search;

    @Schema(description = "Автопарк")
    private UUID autoparkId;

}
