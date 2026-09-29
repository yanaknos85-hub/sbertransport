package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO with change status request
 */

@Getter
@Setter
@Schema(title = "Смена статуса", description = "Данные с новым статусом для заявки")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequestDTO {

    @Schema(description = "Новый статус")
    private String status;
    
}
