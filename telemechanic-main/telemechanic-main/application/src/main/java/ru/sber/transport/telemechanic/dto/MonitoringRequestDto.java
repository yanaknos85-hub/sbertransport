package ru.sber.transport.telemechanic.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.With;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sber.transport.telemechanic.dto.ewb.MonitoringEwbDto;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@With
@Schema(title = "Заявка для мониторинга", description = "Данные заявки для мониторинга")
public record MonitoringRequestDto(
        @Schema(description = "Идентификатор записи о заявке", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @Schema(description = "Человекочитаемый идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
        String humanReadableId,
        @Schema(description = "Дата и время создания заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime creationTime,
        @Schema(description = "Статус заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        RequestStatus requestStatus,
        @Schema(description = "ЭПЛ")
        MonitoringEwbDto ewb,
        @Schema(description = "Автор заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        EmployeeDto author,
        @Schema(description = "Данные автомобиля")
        TransportDto transport,
        @Schema(description = "Статус проверок", requiredMode = Schema.RequiredMode.REQUIRED)
        ChecksStatus checksStatus,
        @Schema(description = "Проверки", requiredMode = Schema.RequiredMode.REQUIRED)
        List<MonitorCheckTreeDto> checks,
        @Schema(description = "Комментарий", maxLength = 255, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 255)
        String comment
) {
}
