package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.magenta.model.EmployeeDTO;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(title = "Заявка на подключение к корп.каршерингу (краткие данные)",
        description = "Заявка на подключение к корп.каршерингу (краткие данные)")
public class GetCarsharingJoinRequestShortDTO {
    
    @Schema(description = "ID заявки")
    private UUID id;
    
    @Schema(description = "Человекочитаемый ID заявки")
    private String humanReadableId;
    
    @Schema(description = "Статус заявки")
    private CarsharingJoinRequestStatus requestStatus;
    
    @Schema(description = "Подключаемый сотудник")
    private EmployeeDTO employee;
}
