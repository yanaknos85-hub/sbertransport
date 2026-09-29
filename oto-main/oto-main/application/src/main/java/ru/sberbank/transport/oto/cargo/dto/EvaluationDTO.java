package ru.sberbank.transport.oto.cargo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.transport.oto.cargo.enums.EvaluationReason;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * DTO with evaluation rating and reasons
 */
@Getter
@Setter
@Schema(title = "Оценка", description = "Данные с оценкой и причинами")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EvaluationDTO {
    
    @NotNull
    @Schema(description = "Идентификатор заявки")
    private UUID requestId;
    
    @Schema(description = "Причина оценки")
    private List<EvaluationReason> reasons;
    
    @Min(1)
    @Max(5)
    @Schema(description = "Оценка")
    private Integer rating;
    
    @Schema(description = "Комментарий")
    @Size(max = 255)
    private String comment;
}
