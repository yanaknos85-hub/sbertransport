package ru.sber.transport.notifications.dto.notification.contractor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@Builder
@Schema(title = "Уведомление контрагента", description = "Данные уведомления контрагента")
public class ContractorNotificationDTO {
    
    @Schema(description = "Название")
    private String name;
    
    @Schema(description = "Текст уведомления")
    private String text;
    
    @Schema(description = "Время создания")
    private LocalDateTime dispatchDateTime;
    
    @Schema(description = "Статус уведомления (просмотрено/не просмотрено)")
    private Boolean viewed;

}
