package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Информация о водителе и транспорте", description = "Информация о водителе и транспорте обслуживающего поездку")
public class DriverInfoDTO {
    
    @Schema(description = "Данные транспорта")
    private String vehicleInfo;
    
    @Schema(description = "Номер телефона водителя")
    private String driverPhone;
    
    @Schema(description = "Имя водителя")
    private String driverName;
    
    @Schema(description = "Гос. номер")
    private String registrationNumber;
}
