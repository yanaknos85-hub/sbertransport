package ru.sber.transport.trip.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Schema(title = "Фильтры для поиска водителей по координатам")
public class DriverLocationSearchDTO extends PageSortFilterParameters<DriverLocationSearchParameters> {

    public DriverLocationSearchDTO(){
        super(DriverLocationSearchParameters.DEADLINE);
    }

    @Schema(description = "Широта")
    private double latitude;

    @Schema(description = "Долгота")
    private double longitude;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Дата начала поездки")
    private LocalDateTime tripStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Дата окончания поездки")
    private LocalDateTime tripEndDate;

    @Schema(description = "Признак ширины поиска")
    private boolean fullSearch = false;

    @Schema(description = "Признак фильтрации по смене")
    private boolean enableShiftFilter = false;

    @Schema(description = "Признак поиска водителей для планирования")
    private boolean forPlanning = false;

    @Schema(description = "ФИО водителя")
    private String name;

    @Schema(description = "Идентификатор филиала")
    private UUID autoparkId;
}
