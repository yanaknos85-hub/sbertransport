package ru.sberbank.ditsib.transport.reports.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Запрос на обновление настроек пользователя")
public class UserPreferencesRequestDTO {

    @Schema(description = "Идентификатор пользователя")
    private UUID userID;

    @Schema(description = "Наименование формы")
    private String nameForm;

    @Schema(description = "Информация о элементах управления ")
    private List<Control> controls;
}
