package ru.sber.transport.trip.business.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.trip.business.model.TripStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Фильтры для экспорта поездок
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TripsExportFiltersDTO {

    /**
     * Временной период
     */
    private Duration duration;

    /**
     * Статсусы поездок
     */
    private List<TripStatus> statuses;

    /**
     * Идентификатор контрагента
     */
    private UUID contractorId;

    @Getter
    @Setter
    public static class Duration{

        /**
         * Начало временного периода
         */
        private OffsetDateTime start;

        /**
         * Конец временного периода
         */
        private OffsetDateTime end;

    }

}
