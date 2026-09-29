package ru.sber.transport.srm.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

/**
 * DTO Адреса маршрута для публикации
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
@Schema(title = "Ошибочный запрос", description = "Атрибуты ошибочного запроса")
public class AddBunchFaultDTO {
    
    /**
     * ID заказа
     */
    private UUID requestId;
    
    /**
     * Сообщение ошибки
     */
    private String message;
    

}
