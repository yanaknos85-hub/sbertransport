package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;

import java.util.List;

@With
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Контейнер для получение проверок заявки по ID заявки", description = "Контейнер для получения проверок заявки по ID заявки")
public class ChecksTreeDto {
    @Schema(description = "Статус возможности связаться с телемехаником")
    private boolean callTelemech;
    @NotNull
    @Schema(description = "Гос.номер ТС",
            example = "А123АА777")
    private String stateNumber;
    @Schema(description = "Пробег ТС",
            example = "123456",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true)
    private Integer mileage;
    @Schema(description = "Проверки")
    private ChecksTree checks;
    
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChecksTree {
        @Schema(description = "Проверки, которые надо пройти")
        List<CheckDto> pass;
        @Schema(description = "Завершенные проверки")
        List<CheckDto> finished;
    }
    
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckDto {
        @Schema(description = "Тип проверки")
        CheckType checkType;
        @Schema(description = "Статус проверки")
        CheckStatus checkStatus;
        @Schema(description = "Кол-во сделанных попыток пройти проверку")
        int attempt;
        @Schema(description = "Кол-во попыток пройти проверку")
        int maxAttempt;
        @Schema(description = "Комментарий в случае отклонения проверки телемехаником")
        String comment;
        @Schema(description = "Дочерние проверки")
        List<CheckDto> children;
    }
}
