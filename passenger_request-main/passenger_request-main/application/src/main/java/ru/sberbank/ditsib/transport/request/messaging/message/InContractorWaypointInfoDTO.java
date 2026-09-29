package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

/**
 * Входящая от исполнителя информация о точке маршрута
 * @deprecated use kafka-functional
 */
@ToString
@Getter
@EqualsAndHashCode
@Jacksonized
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Deprecated(forRemoval = true)
public class InContractorWaypointInfoDTO {
    /**
     * Порядковый номер точки маршрута
     */
    private int id;
    
    /**
     * Фактическое время прибытия машины в точку
     */
    private LocalDateTime factTimeArrive;
}
