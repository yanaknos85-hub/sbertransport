package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * ДТО с информацией о назначенном авто
 */
@Getter
@Setter
@Schema(title = "Водитель назначен", description = "Данные о назначенном авто")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignedDTO {
    
    @NotBlank
    @Schema(description = "Данные о назначенном авто")
    private String vehicleInfo;
}
