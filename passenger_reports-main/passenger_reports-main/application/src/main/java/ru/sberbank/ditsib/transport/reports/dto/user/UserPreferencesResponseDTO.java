package ru.sberbank.ditsib.transport.reports.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
@Schema(description = "Информация по настройкам пользователя")
public class UserPreferencesResponseDTO {

    @Schema(description = "Иденитификатор пользователя")
    private UUID userID;

    @Schema(description = "Наименование формы")
    private String nameForm;

    @Schema(description = "Код результата обработки")
    private Integer statusCode;

    @Schema(description = "Описание ошибки")
    private String statusDescription;

    @Schema(description = "Информация о элементах управления")
    private List<Control> controls;
}
