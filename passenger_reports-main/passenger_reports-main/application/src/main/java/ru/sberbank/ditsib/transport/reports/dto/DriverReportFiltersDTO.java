package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "Фильтры для реестра водителей")
public class DriverReportFiltersDTO {
    
    @Schema(description = "Флаг \"С учетом фильтров\"")
    private Boolean withFilters;
    
    @Schema(description = "Поля для флага \"С учетом фильтров\"")
    private DriverUIVisibilityDTO driverUIVisibilityDTO;
    
    @Schema(description = "Имя")
    private String firstName;
    
    @Schema(description = "Фамилия")
    private String lastName;
    
    @Schema(description = "Отчество")
    private String patronymic;
    
    @Schema(description = "Статусы водителя")
    private String driverStatuses;
    
    @Schema(description = "Автопарки")
    private String autoparkNames;
    
    @Schema(description = "Признаки водителей")
    private String driverTags;
    
    @Schema(description = "Опыт вождения")
    private String driverExp;
    
    @Schema(description = "Рейтинг водителя")
    private Integer driverRating;
    
}