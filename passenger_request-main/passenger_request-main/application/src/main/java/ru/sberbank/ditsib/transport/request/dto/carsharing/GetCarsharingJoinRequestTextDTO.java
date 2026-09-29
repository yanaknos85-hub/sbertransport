package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Текстовые поля заявки на подключение к корп.каршерингу",
        description = "Поле до ФИО разбито на три части. Вторая часть - значение КС - берется из настроек")
public class GetCarsharingJoinRequestTextDTO extends UpdateCarsharingJoinRequestTextDTO {
    
    @Schema(description = "Вторая часть текстового поля до ФИО - значение КС, берется из настроек КС")
    private String beforeFioSecondPart;
}
