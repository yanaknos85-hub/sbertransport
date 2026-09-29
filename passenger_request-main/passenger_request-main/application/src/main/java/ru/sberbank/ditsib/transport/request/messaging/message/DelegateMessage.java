package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Соообщение: сведения о делегате.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DelegateMessage implements Message<UUID> {
    
    /**
     * ID записи
     */
    private UUID id;
    
    /**
     * Идентификатор руководителя
     */
    private UUID supervisorId;
    
    /**
     * ID пользователя которому делегированы полномочия
     */
    private UUID delegateId;
    
    /**
     * Дата начала делегирования полномочий
     */
    private LocalDate startDate;
    
    /**
     * Дата окончания делегирования полномочий
     */
    private LocalDate endDate;
  
    
    /**
     * ID типа транспорта.
     */
    private UUID transportTypeId;
    
    /**
     * Flag of organization deleted.
     */
    @Builder.Default
    private boolean deleted = false; // TODO Medvedev_AD
}
