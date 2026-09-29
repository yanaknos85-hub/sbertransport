package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "MonitoringEwbInfo", title = "Информация об ЭПЛ для мониторинга заявок телемеханика")
public record MonitoringEwbDto(
        @Schema(description = "Идентификатор ЭПЛ")
        UUID ewbId,
        @Schema(description = "Человекочитаемый идентификатор ЭПЛ")
        String humanReadableId,
        @Schema(description = "Идентификатор ЭПЛ в системе КОРУС")
        UUID ewbUuid,
        @Schema(description = "Статус")
        EwbStatus status,
        @Schema(description = "Дата путевого листа")
        LocalDate startDate,
        @Schema(description = "Дата окончания путевого листа")
        LocalDate finishDate,
        @Schema(description = "Тип титула, который необходимо сформировать и подписать")
        EwbTitleType nextTitleType
) {
}
