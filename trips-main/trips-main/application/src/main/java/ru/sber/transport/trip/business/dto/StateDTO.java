package ru.sber.transport.trip.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Состояние", description = "Состояние признака \"на линии\" у водителя")
public class StateDTO {

    /**
     * Состояние
     */
    @NotNull
    @Schema(title = "Состояние", description = "Состояние признака \"на линии\" у водителя")
    private boolean state;
}