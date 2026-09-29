package ru.sber.transport.dispatcher.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.sberbank.ditsib.request.PageSortFilterParameters;
import ru.sberbank.ditsib.validation.interval.Interval;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(title = "Поиск смены", description = "Поиск смены")
@Interval(startField = "startDate", endField = "endDate", inclusion = Interval.Include.INCLUDE)
public class ShiftSearchDto extends PageSortFilterParameters<ShiftSearchParametres> {

    public ShiftSearchDto(){super(ShiftSearchParametres.START_DATE);}

    @Schema(description = "Дата начала смены")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @Schema(description = "Дата окончания смены")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

}
