package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(title = "Новая заявка на подключение к корп.каршерингу",
        description = "Новая заявка для отправки на сервер (не содержит текстовых полей и id)")
public class UpdateCarsharingJoinRequestDTO {
    
    @Schema(description = "Номер телефона, с которым сотрудник зарегистрирован в каршеринговом сервисе", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String phone;
    
    @Schema(description = "Почта, с которой сотрудник зарегистрирован в каршеринговом сервисе", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String email;
    
    @Schema(description = "Подключаемые корп.каршеринги со статусами подключения", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Set<@Valid ContractorAndJoinStatusDTO> contractors;
}
