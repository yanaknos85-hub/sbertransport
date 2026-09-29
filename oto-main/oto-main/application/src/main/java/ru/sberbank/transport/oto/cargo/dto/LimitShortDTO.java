package ru.sberbank.transport.oto.cargo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.limits.LimitSharingType;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Краткая информация по лимиту", description = "Краткая информация по лимиту")
public class LimitShortDTO {
    

    /**
     * Идентификатор
     */
    @Schema(description = "Идентификатор лимита")
    UUID id;

    /**
     * Человекочитаемый идентификатор
     */
    @Schema(description = "Человекочитаемый идентификатор")
    private String humanReadableId;


    /**
     * Тип лимита DEPARTMENT\EMPLOYEE
     */
    @Schema(description = "Тип лимита (DEPARTMENT | EMPLOYEE)")
    private LimitType limitType;

    /**
     * Тип распределения лимита MONTHLY\QUARTER\PERCENTS
     */
    @Schema(description = "Тип распределения лимита (MONTHLY | QUARTER | PERCENTS)")
    private LimitSharingType limitSharingType;

}
