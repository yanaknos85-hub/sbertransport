package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Настройки лимитов", description = "Настройки лимитов")
public class GetCheckinSettingsDTO {

    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Service type
     */
    @Schema(description = "Услуга")
    private TransportServiceType serviceType;
    
    /**
     * Transport type
     */
    @Schema(description = "Тип транспорта")
    private TransportTypeEnum transportType;
    
    /**
     * region
     */
    @Schema(description = "Регион")
    private String region;
    
    /**
     * radius
     */
    @Schema(description = "Радиус")
    private Integer radius;
}
