package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

/**
 * ДТО с доп информацией при смене статуса
 */
@Getter
@Setter
@Schema(title = "Смена статуса", description = "комментарий при смене статуса")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusDTO {
    
    @NotBlank
    @Schema(description = "Комментарий при отмене заявки инженером")
    @Length(max = 255)
    private String comment;
}
