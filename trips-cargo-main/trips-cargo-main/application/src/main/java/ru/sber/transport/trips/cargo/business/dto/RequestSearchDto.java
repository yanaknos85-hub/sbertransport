package ru.sber.transport.trips.cargo.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Объект поиска данных о поездках.
 */
@Setter
@Getter
public class RequestSearchDto extends PageSortFilterParameters<RequestSearchParameters> {

    @Schema(title = "Идентификатор", description = "Человекочитаемый идентификатор заявки")
    private String requestHumanReadableId;

    @Schema(title = "Идентификатор", description = "Человекочитаемый идентификатор поездки")
    private String humanReadableId;

    @Schema(title = "Время", description = "Желаемое время начала поездки")
    private OffsetDateTime desireDateStart;

    @Schema(title = "Время", description = "Желаемое время окончания поездки")
    private OffsetDateTime desireDateEnd;

    @Schema(title = "Cписок идентификаторов", description = "Cписок идентификаторов водителей")
    private List<UUID> driverIds;

    @Schema(description = "Идентификатор филиала")
    private UUID autoparkId;

    /**
     * Создать новый объект.
     */
    protected RequestSearchDto() {
        super(RequestSearchParameters.HUMAN_READABLE_ID);
    }
}
