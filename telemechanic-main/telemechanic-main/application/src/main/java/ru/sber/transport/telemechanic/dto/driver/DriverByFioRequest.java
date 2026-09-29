package ru.sber.transport.telemechanic.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import ru.sber.transport.telemechanic.dto.PageSettingDto;

import java.util.UUID;

@Schema(description = "Поиск водителя по ФИО")
public record DriverByFioRequest(
        @Schema(description = "Текст поиска", requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 3, maxLength = 100, example = "ива")
        @Size(min = 3, max = 100)
        String searchText,
        @Schema(description = "Идентификатор транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID transportId,
        @Schema(description = "Настройка пагинации", requiredMode = Schema.RequiredMode.REQUIRED)
        PageSettingDto pageSetting
) {

    public PageRequest preparePageRequest() {
        return pageSetting == null ? PageRequest.of(0, 10) :
                 PageRequest.of(pageSetting().page(), pageSetting().size());
    }
}
