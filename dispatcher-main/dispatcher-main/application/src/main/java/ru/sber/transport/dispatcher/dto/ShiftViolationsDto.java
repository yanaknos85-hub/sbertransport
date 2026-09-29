package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@Schema(title = "Данные об отклонениях", description = "Данные об отклонениях в смене")
public class ShiftViolationsDto extends ShiftDTO{

    private Object body;

}
