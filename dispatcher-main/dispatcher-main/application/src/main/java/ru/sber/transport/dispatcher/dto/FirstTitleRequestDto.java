package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirstTitleRequestDto {

    @Schema(description = "Список идентификаторов смен")
    @NotEmpty
    @Size(max = 100, message = "Количество элементов списка не должно превышать 100")
    private List<UUID> shiftIds;

    @Schema(description = "Часовой пояс")
    @NotNull
    @NotEmpty
    private String timeZone;

}
