package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO для Информации по каршерингу ответ
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "Информация по каршерингу", description = "Информация по каршерингу ответ")
public class CarsharingInfoResponseDTO {
    
    @Schema(name = "Согласие на обработку персональных данных")
    private boolean consent;
    
    @Schema(name = "Пользовался ли клиент уже каршерингом через банк")
    private boolean previouslyUsed;
    
    @Schema(name = "Диплинк")
    private String deepLink;
    
}
