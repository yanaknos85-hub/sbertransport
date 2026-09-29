package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.util.List;
import java.util.UUID;

@Schema(name = "PatchMonitoringResponse", title = "Контейнер измененной заявки телемеханика")
public record PatchMonitoringResponse(
        @Schema(description = "Идентификатор заявки")
        UUID id,
        @Schema(description = "Статус заявки")
        RequestStatus requestStatus,
        @Schema(description = "Комментарий к заявке")
        String comment,
        @Schema(description = "Список проверок")
        List<MonitorCheckTreeDto> checks
) {
}
