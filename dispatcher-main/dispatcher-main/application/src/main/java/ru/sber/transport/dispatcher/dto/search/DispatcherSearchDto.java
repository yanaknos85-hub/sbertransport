package ru.sber.transport.dispatcher.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

import java.util.UUID;

@Getter
@Setter
@Schema(title = "Поиск и пагинация диспетчеров")
public class DispatcherSearchDto extends PageSortFilterParameters<DispatcherSearchParameters> {

    public DispatcherSearchDto() {
        super(DispatcherSearchParameters.LAST_NAME);
    }

    @Schema(description = "Имя")
    private String firstName;

    @Schema(description = "Фамилия")
    private String lastName;

    @Schema(description = "Отчество")
    private String patronymic;

    @Schema(description = "Признак активности")
    private Boolean active;

    @Schema(description = "Автопарк")
    private UUID autoparkId;
}
