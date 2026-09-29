package ru.sberbank.transport.oto.cargo.dto.cargo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.request.enums.RequestTypeEnum;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.transport.oto.cargo.enums.CriticalExpireDateEnum;
import ru.sberbank.transport.oto.cargo.enums.SortDirection;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Schema(description = "DTO для фильтрации заявок на грузоперевозки (запрос от инженера ОТО)")
public record CargoRequestDto(

        @Schema(description = "Идентификатор организации", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID organizationId,

        @Schema(description = "Размер страницы", defaultValue = "10", example = "20")
        Integer pageSize,

        @Schema(description = "Номер страницы", defaultValue = "0", example = "1")
        Integer page,

        @Schema(description = "Направление сортировки", allowableValues = {"ASC", "DESC"}, defaultValue = "DESC")
        SortDirection sortDirection,

        @Schema(description = "Поле сортировки", example = "creationTime")
        String sortField,

        @Schema(description = "Идентификатор заявки", example = "REQ123456")
        String id,

        @Schema(description = "Статусы заявки")
        List<TripRequestStatus> statuses,

        @Schema(description = "Адрес отправления", example = "ул. Ленина, д. 1")
        String senderAddress,

        @Schema(description = "Адрес получения", example = "ул. Пушкина, д. 2")
        String recipientAddress,

        @Schema(description = "ФИО отправителя", example = "Иванов Иван Иванович")
        String senderName,

        @Schema(description = "ФИО получателя", example = "Петров Петр Петрович")
        String recipientName,

        @Schema(description = "Дата создания заявки, с", type = "string", format = "date-time", example = "2024-01-01T10:00:00Z")
        ZonedDateTime creationTimeFrom,

        @Schema(description = "Дата создания заявки, по", type = "string", format = "date-time", example = "2024-01-31T23:59:59Z")
        ZonedDateTime creationTimeTo,

        @Schema(description = "Дата доставки, с", type = "string", format = "date-time", example = "2024-02-01T08:00:00Z")
        ZonedDateTime desiredTimeFrom,

        @Schema(description = "Дата доставки, по", type = "string", format = "date-time", example = "2024-02-01T18:00:00Z")
        ZonedDateTime desiredTimeTo,

        @Schema(description = "Контрольная дата с учетом выходных дней, с", type = "string", format = "date-time", example = "2024-01-15T09:00:00Z")
        ZonedDateTime controlTimeFrom,

        @Schema(description = "Контрольная дата с учетом выходных дней, по", type = "string", format = "date-time", example = "2024-01-16T17:00:00Z")
        ZonedDateTime controlTimeTo,

        @Schema(description = "Дата согласования, с", type = "string", format = "date-time", example = "2024-01-15T09:00:00Z")
        ZonedDateTime approvalTimeFrom,

        @Schema(description = "Дата согласования, по", type = "string", format = "date-time", example = "2024-01-16T17:00:00Z")
        ZonedDateTime approvalTimeTo,

        @Schema(description = "Дата и время сбора (фактическое), с", type = "string", format = "date-time", example = "2024-02-01T07:00:00Z")
        ZonedDateTime transferTimeFrom,

        @Schema(description = "Дата и время сбора (фактическое), по", type = "string", format = "date-time", example = "2024-02-01T08:00:00Z")
        ZonedDateTime transferTimeTo,

        @Schema(description = "Дата и время доставки (фактическое), с", type = "string", format = "date-time", example = "2024-02-01T12:00:00Z")
        ZonedDateTime shipmentTimeFrom,

        @Schema(description = "Дата и время доставки (фактическое), по", type = "string", format = "date-time", example = "2024-02-01T14:00:00Z")
        ZonedDateTime shipmentTimeTo,

        @Schema(description = "Подразделение инициатора (автора) заявки", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID authorDepartment,

        @Schema(description = "Тип транспорта", allowableValues = {"CAR", "VAN", "TRUCK"})
        TransportTypeEnum transportType,

        @Schema(description = "Типы заявки")
        List<RequestTypeEnum> requestTypes,

        @Schema(description = "UUID групп исполнителей")
        Set<UUID> executorGroupIds,

        @Schema(description = "Уровень критичности даты истечения срока")
        CriticalExpireDateEnum criticalExpireDate,

        @Schema(description = "Флаг фильтрации заявок без группы исполнителя", example = "true")
        @JsonProperty("emptyExecutorGroup")
        boolean isEmptyExecutorGroup
) {
        public Integer getOrDefaultPageSize() {
            return Objects.requireNonNullElse(pageSize, 10);
        }

        public Integer getOrDefaultPage() {
                return Objects.requireNonNullElse(page, 0);
        }

        public SortDirection getOrDefaultSortDirection() {
                return Objects.requireNonNullElse(sortDirection, SortDirection.DESC);
        }

        public String getOrDefaultSortField() {
                return Objects.requireNonNullElse(sortField, "creationTime");
        }
}
