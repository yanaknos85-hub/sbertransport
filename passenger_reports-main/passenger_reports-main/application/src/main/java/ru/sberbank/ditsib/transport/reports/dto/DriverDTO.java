package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Информация о водителе", description = "Информация о водителе")
public class DriverDTO {
    
    @Schema(description = "Имя")
    private String firstName;
    
    @Schema(description = "Фамилия")
    private String lastName;
    
    @Schema(description = "Отчество")
    private String patronymic;
    
    @Schema(description = "Контактный номер телефона")
    private String contactPhone;
    
    @Schema(description = "Рейтинг водителя 0-500")
    private Integer rating;
    
}
