package ru.sber.transport.telemechanic.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.LocalDateTime;
import java.util.UUID;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

@Schema(title = "Список заявок для мониторинга", description = "Данные списка заявок для мониторинга")
public record MonitoringRequestListDto(
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

    @Schema(description = "Государственный номер", requiredMode = RequiredMode.REQUIRED)
    String stateNumber,

    @Schema(description = "Наименование организации", requiredMode = RequiredMode.REQUIRED)
    String organizationName,

    @Schema(description = "Статус проверок", requiredMode = Schema.RequiredMode.REQUIRED)
    ChecksStatus checksStatus
) {

}
