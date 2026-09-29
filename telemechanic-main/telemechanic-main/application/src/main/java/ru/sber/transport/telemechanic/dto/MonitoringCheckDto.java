package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Данные о проверке", name = "monitoringCheckDto", description = "Данные о проверке")
public class MonitoringCheckDto {
        @Schema(description = "Идентификатор записи о проверке", requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID id;
        @Schema(description = "Тип проверки", requiredMode = Schema.RequiredMode.REQUIRED)
        private CheckType checkType;
        @Schema(description = "Статус проверки", requiredMode = Schema.RequiredMode.REQUIRED)
        private CheckStatus checkStatus;
        @Schema(description = "Количество попыток пройти проверку", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer attempt;
        @Schema(description = "Максимальное количество попыток пройти проверку", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer maxAttempt;
        @Schema(description = "Фотограции проверок", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<UUID> photos;
        @Schema(description = "Комментарий")
        private String comment;
        @Schema(description = "Дочерние проверки")
        private List<MonitoringCheckDto> children;
}
