package ru.sber.transport.dispatcher.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

import java.util.UUID;

@Getter
@Setter
@Schema(title = "Поиск автопарка", description = "Фильтры для автопарка")
public class AutoparkSearchDTO extends PageSortFilterParameters<AutoparkSearchParameters> {


    public AutoparkSearchDTO() {super(AutoparkSearchParameters.NAME);}

    @Schema(description = "Название автопарка")
    private String name;

    @Schema(description = "Активность автопарка")
    private Boolean active;

    @Schema(description = "Идентификатор подразделения")
    private UUID routingId;

}
