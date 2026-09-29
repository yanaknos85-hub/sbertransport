package ru.sber.transport.trips.cargo.business.dto;

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
@Schema(title = "Состояние", description = "Состояние")
public class StateDTO {

    /**
     * Состояние
     */
    @NotNull
    @Schema(description = "Состояние")
    private boolean state;
}
