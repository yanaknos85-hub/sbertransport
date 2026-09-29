package ru.sberbank.ditsib.transport.reports.dto.oto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "Адрес", description = "Данные адреса")
public class AddressDto {
    
    @Schema(title = "Страна")
    private final String country;
    
    @Schema(title = "Регион")
    private final String region;
    
    @Schema(title = "Город")
    private final String city;
    
    @Schema(title = "Улица")
    private final String street;
    
    @Schema(title = "Дом")
    private final String house;
    
    @Schema(title = "Корпус")
    private final String building;
    
    @Schema(title = "Строение")
    private final String structure;
    
    @Override
    public String toString() {
        return region + ", " + street + ", " + house;
    }
}
