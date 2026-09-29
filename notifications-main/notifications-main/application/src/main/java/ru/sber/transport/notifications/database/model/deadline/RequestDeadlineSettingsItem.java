package ru.sber.transport.notifications.database.model.deadline;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import jakarta.persistence.*;

/** Сущность - настройка контрольного срока для конкретного типа транспорта */
@Entity
@DiscriminatorValue(value = "REQUEST_ITEM")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDeadlineSettingsItem extends DeadlineSettingsItem{
    
    /** Соответствующий тип транспорта, для которого создана настройка */
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type")
    private TransportTypeEnum transportType;
    
    /** Соответствующий статус заявки, для которого создана настройка */
    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private TripRequestStatus requestStatus;
}

