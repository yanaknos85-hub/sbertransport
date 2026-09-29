package ru.sber.transport.notifications.dto.notification.contractor;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.validation.interval.NotNullValue;

import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
@Schema(title = "Поиск уведомлений контрагента", description = "Фильтры для поисков уведомлений контрагента")
public class ContractorNotificationSearchDTO {
    
    @Schema(description = "Название автопарка")
    private String autoparkName;
    
    @Schema(description = "Человекочитаемый id заявки")
    private String requestId;
    
    @Schema(description = "ФИО водителя")
    private String driverFIO;
    
    @Schema(description = "Дата и время создания уведомления")
    private DateRange dispatchDateTime;
    
    @Schema(description = "Статус уведомления (просмотрено/не просмотрено)")
    private Boolean viewed;
    
    @Schema(description = "ФИО ответственного диспетчера")
    private String dispatcherFIO;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @NotNullValue(fields = { "start", "end"})
    public static class DateRange {
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        private LocalDateTime start;
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        private LocalDateTime end;
    }
    
}
