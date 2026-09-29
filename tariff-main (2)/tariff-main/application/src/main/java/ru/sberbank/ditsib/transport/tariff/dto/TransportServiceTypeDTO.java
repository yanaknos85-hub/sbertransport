package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(title = "Тип транспорта", description = "Вид транспортной услуги")
public class TransportServiceTypeDTO {
    /**
     * Имя
     */
    @Schema(description = "Название")
    private final String name;
    
    /**
     * Русское описание
     */
    @Schema(description = "Русское описание для отображения")
    private final String rusName;
}
