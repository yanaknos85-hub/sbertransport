package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Schema(title = "Поиск водителя", description = "Фильтры для поиска водителя")
public class DriverSearchDTO {
    
    @Schema(description = "Настройки сортировки водителей")
    private DriverSortSetting driverSortSetting;
    
    @Schema(description = "Настройки разделения на страницы")
    private PageSetting pageSetting;
    
    @Getter
    @Setter
    @ToString
    public static class DriverSortSetting {
        
        @Schema(description = "Выбор сортировки", defaultValue = "null")
        private DriverSortOption property = DriverSortOption.FIRST_NAME;
        
        @Schema(description = "Направление сортировки", defaultValue = "true")
        private boolean directionAsc = true;
    }
    
    @Getter
    @Setter
    @ToString
    public static class PageSetting {
        @Schema(description = "Номер страницы", defaultValue = "0")
        private int page = 0;
        
        @Schema(description = "Количество элементов на странице", defaultValue = "20")
        private int size = 20;
    }
    
}
