package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Оценка", description = "Оценка выполненной заявки")
public class RequestRatingDTO {
    
    @Builder.Default
    @Schema(description = "Список понравившегося")
    private final Set<String> advantages = new HashSet<>();
    
    @Builder.Default
    @Schema(description = "Список недостатков")
    private final Set<String> drawbacks = new HashSet<>();
    
    @Min(0)
    @Max(5)
    @Schema(description = "Оценка поездки")
    private Integer rating;
    
    @Schema(description = "Комментарий")
    private String ratingComment;
}
