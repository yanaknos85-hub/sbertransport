package ru.sberbank.ditsib.transport.reports.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Schema(description = "Информация о свойствах элемента управления")
@AllArgsConstructor
public class Settings {
    
    @Schema(description = "Наименование свойства")
    private String nameSetting;
    
    @Schema(description = "Значение свойства элемента управления")
    private String valueSetting;
    
    @Schema(description = "Информация о порядке сортировки")
    private Integer sort;
}
