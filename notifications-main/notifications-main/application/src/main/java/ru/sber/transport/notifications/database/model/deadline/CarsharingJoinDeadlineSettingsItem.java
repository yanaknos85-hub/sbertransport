package ru.sber.transport.notifications.database.model.deadline;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

/** Сущность - настройка контрольного срока для конкретного типа лимита */
@Entity
@DiscriminatorValue(value = "CARSHARING_JOIN_ITEM")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CarsharingJoinDeadlineSettingsItem extends DeadlineSettingsItem {
    
    /** Статус заявки на подключение к корп. каршерингу */
    @Enumerated(EnumType.STRING)
    @Column(name = "carsharing_join_status")
    private CarsharingJoinRequestStatus carsharingJoinStatus;
    
    /** Соответствующий тип транспорта, для которого создана настройка */
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type")
    private TransportTypeEnum transportType;
}
