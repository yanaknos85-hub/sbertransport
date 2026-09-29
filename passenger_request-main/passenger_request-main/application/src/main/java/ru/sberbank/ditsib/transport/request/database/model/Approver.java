package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;

/**
 * Содержит в себе подразделение и тип транспорта, заявки которые может согласовывать сотрудник
 */
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
@Getter
@EqualsAndHashCode
public class Approver {
    @Column(name = "employee_id")
    private UUID employeeId;
    
    /**
     * Ограничение на согласовние по типу транспорта (null для head, заполнено для делегата)
     */
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
}
