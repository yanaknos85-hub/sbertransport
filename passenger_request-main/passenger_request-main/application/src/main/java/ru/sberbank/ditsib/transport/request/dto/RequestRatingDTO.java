package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * DTO for rating request
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Оценка", description = "Оценка выполненной заявки")
public class RequestRatingDTO {
    /**
     * Advantages of request
     */
    @Builder.Default
    @Schema(description = "Список понравившегося")
    private final Set<String> advantages = new HashSet<>();
    
    /**
     * Drawbacks of request
     */
    @Builder.Default
    @Schema(description = "Список недостатков")
    private final Set<String> drawbacks = new HashSet<>();
    /**
     * Rating
     */
    @Min(0)
    @Max(5)
    @Schema(description = "Оценка поездки")
    private Integer rating;
    /**
     * Comment
     */
    @Schema(description = "Комментарий")
    private String ratingComment;
}
