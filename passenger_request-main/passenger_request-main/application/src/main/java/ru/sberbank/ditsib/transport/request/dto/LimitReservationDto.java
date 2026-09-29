package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Object with data about action.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LimitReservationDto {
    
    /**
     * Identifier
     */
    @NotNull
    private UUID organizationId;
    
    /**
     * Limit owner
     */
    @NotNull
    private UUID departmentId;
    
    /**
     * Limit owner
     */
    @NotNull
    private UUID employeeId;
    
    /**
     * Limit sharing type
     */
    @NotNull
    private TransportTypeEnum transportType;
    
    /**
     * Sum
     */
    @NotNull
    private Long sum;
    
    /**
     * Bonus sum
     */
    private Long bonusSum;
    
    /**
     * Date and time or request creation
     */
    @NotNull
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime plannedDate;
    
    /**
     * Limit limit
     */
    private UUID requestId;
    
    /**
     * Человекочитаемый идентификатор
     */
    private String humanReadableId;
    
    /**
     * Признак совместной поездки
     */
    @Builder.Default
    private boolean coop = false;
    
    /**
     * Признак водителя
     */
    @Builder.Default
    private boolean driver = false;
    
    /**
     * Проверка лимита.
     */
    @Builder.Default
    private boolean checkLimit = false;
}