package ru.sber.transport.telemechanic.dto.transport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import ru.sber.transport.telemechanic.dto.PageSettingDto;

import java.util.Objects;

@Schema(name = "GetTransportRequest", title = "Запрос на поиск транспорта",
        description = "Запрос на поиск транспорт по гос. номеру")
public record GetTransportRequest(
        @Size(min = 3, max = 9, message = "Государственный номер не должен быть меньше 3 и больше 9 символов")
        @Schema(description = "Государственный номер",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "А777АА77",
                nullable = true,
                minimum = "3",
                maximum = "9")
        String stateNumber,
        
        @Valid
        @NotNull
        @Schema(description = "Настройки пагинации",
                requiredMode = Schema.RequiredMode.REQUIRED)
        PageSettingDto pageSetting

) {
    public PageRequest preparePageRequest() {
        return Objects.nonNull(pageSetting) ? PageRequest.of(pageSetting().page(), pageSetting().size())
                                            : PageRequest.of(0, 10);
    }
    
}
