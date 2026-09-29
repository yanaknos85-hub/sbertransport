package ru.sberbank.transport.oto.cargo.dto.oto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@Schema(title = "Маршрут (монитор)", description = "Данные шаблона регулярной доставки для монитора")
public class GetTemplateForCargoForOtoDto {
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
    
    @Schema(description = "Статус")
    private String status;
    
    @Schema(description = "Тип транспорта")
    private String tariffType;
    
    @Schema(description = "Дата создания следующей заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime nextRequestDate;
    
    @Schema(description = "Дата последней заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime lastRequestDate;
    
    @Schema(description = "Периодичность расписания")
    private String period;
    
    @Schema(description = "ФИО заявителя")
    private String author;
    
    @Schema(description = "Телефон заявителя")
    private String authorMobilePhone;
    
    @Schema(description = "Перевозчик")
    private String carrier;
    
    @Schema(description = "Адрес отправления")
    private String senderAddress;
    
    @Schema(description = "ФИО отправителя")
    private String sender;
    
    @Schema(description = "Телефон отправителя")
    private String senderPhone;
    
    @Schema(description = "Организация отправитель")
    private String senderOrganization;
    
    @Schema(description = "Адрес получателя")
    private String recipientAddress;
    
    @Schema(description = "ФИО получателя")
    private String recipient;
    
    @Schema(description = "Телефон получателя")
    private String recipientPhone;
    
    @Schema(description = "Организация получатель")
    private String recipientOrganization;
    
    @Schema(description = "Плановая дальность")
    private Double plannedRange;
    
    @Schema(description = "Плановая стоимость")
    private Long plannedPrice;
    
    @Schema(description = "Вид груза")
    private String cargoType;
    
    @Schema(description = "Количество грузчиков")
    private Integer loaders;
    
    @Schema(description = "Объем, м3")
    private Double volume;
    
    @Schema(description = "Вес, кг.")
    private Double weight;
    
    @Schema(description = "Дата создания расписания")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime creationTime;
    
    @Schema(description = "Комментарий")
    private String comment;

    @Schema(description = "Количество заявок созданных целиком по расписанию")
    private Integer countRequests;

    @Schema(description = "Количество заявок в рамках одного маршрута (шаблона)")
    private Integer countRequestsInRoute;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder(toBuilder = true)
    @Schema(title = "Периодичность")
    public static class CargoPeriodDto {
        
        @Schema(description = "Периодичность", requiredMode = Schema.RequiredMode.REQUIRED)
        private CargoPeriodEnum periodType;
        
        @Schema(description = "День недели")
        private Set<Integer> dayOfWeek;
        
        @Schema(description = "Неделя месяца")
        private Set<Integer> weekOfMonth;
        
        @Schema(description = "Месяц в квартале")
        private Set<Integer> monthOfQuartal;
        
        private Long cost;
        
        @RequiredArgsConstructor
        @Getter
        @Schema(title = "Период действия", description = "Доступные периоды действия расписания")
        public enum CargoPeriodEnum {
            WEEK("Еженедельно"),
            MONTH("Ежемесячно"),
            QUARTER("Поквартально");
            
            private final String description;
        }
    }
}
