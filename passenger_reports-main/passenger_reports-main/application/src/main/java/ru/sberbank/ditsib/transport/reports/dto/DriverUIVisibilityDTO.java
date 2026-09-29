package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


/**
 * Настройки видимости водителей.
 */
@Getter
@Setter
@Schema(title = "Данные аттрибутов водителей",
        description = "Измененные данные атрибутов водителей")
public class DriverUIVisibilityDTO {
    
    /**
     * Видимость столбца Статусы водителя
     */
    @NotNull
    @Schema(description = "Статусы водителя", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean driverStatuses;
    
    /**
     * Видимость столбца Автопарк
     */
    @NotNull
    @Schema(description = "Автопарк", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean autoparkNames;
    
    /**
     * Видимость столбца Имя
     */
    @NotNull
    @Schema(description = "Имя", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean firstName;
    
    /**
     * Видимость столбца Фамилия
     */
    @NotNull
    @Schema(description = "Фамилия", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean lastName;
    
    /**
     * Видимость столбца Отчество
     */
    @NotNull
    @Schema(description = "Отчество", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean patronymic;
    
    /**
     * Видимость столбца Признаки водителя
     */
    @NotNull
    @Schema(description = "Признаки водителя", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean driverTags;
    
    /**
     * Видимость столбца Опыт вождения
     */
    @NotNull
    @Schema(description = "Опыт вождения", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean driverExp;
    
    /**
     * Видимость столбца Рейтинг водителя
     */
    @NotNull
    @Schema(description = "Рейтинг водителя", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean driverRating;
    
}