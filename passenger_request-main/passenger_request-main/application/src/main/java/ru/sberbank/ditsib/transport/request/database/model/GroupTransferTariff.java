package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;

/**
 * Cущность тарифа, получаемая из сообщения
 */
@Getter
@Entity
@Setter
@DiscriminatorValue(value = TransportTypeEnum.Constants.GROUP_TRANSFER_STRING)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GroupTransferTariff extends BaseTariffWithContract {
    
    @Column(name = "contractor_id")
    private UUID contractorId;
    
    @Column(name = "contractor_tariff_id")
    private String contractorTariffId;
    
    //Класс группового трансфера
    @NotNull
    @Column(name = "taxi_class")
    @Enumerated(EnumType.STRING)
    private GroupTransferClass groupTransferClass;
    
    //Минимальное время для формирования заказа в минутах.
    @Column(name = "min_create_time")
    @Builder.Default
    private int minCreateTime = 0;
    
    //Минимальное время отмены поездки в минутах
    @Builder.Default
    @Column(name = "min_cancel_time")
    private int minCancelTime = 0;
    
    //Триггерное время
    @Column(name = "trigger_time")
    @Builder.Default
    private int triggerTime = 60;
    
}
