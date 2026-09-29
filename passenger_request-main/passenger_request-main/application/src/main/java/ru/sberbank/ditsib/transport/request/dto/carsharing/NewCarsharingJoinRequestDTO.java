package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(title = "Новая заявка на подключение к корп.каршерингу",
        description = "Новая заявка для отправки на сервер (не содержит текстовых полей и id)")
public class NewCarsharingJoinRequestDTO extends UpdateCarsharingJoinRequestDTO {
    
    @Schema(description = "Согласие с правилами Памятка П-144", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private boolean rulesP144Agree;
    
    @Schema(description = "Согласие на обработку персональных данных", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private boolean personalDataAgree;
}
