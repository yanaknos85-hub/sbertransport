package ru.sberbank.ditsib.transport.reports.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Schema(description = "Информация о элементах управления ")
@AllArgsConstructor
@NoArgsConstructor
public class Control {

    @Schema(description = "Тип элемента управления")
    private String typeControl;

    @Schema(description = "Наименование элемента управления")
    private String value;

    @Schema(description = "Информация о свойствах элемента управления")
    private List<Settings> settings;
}
