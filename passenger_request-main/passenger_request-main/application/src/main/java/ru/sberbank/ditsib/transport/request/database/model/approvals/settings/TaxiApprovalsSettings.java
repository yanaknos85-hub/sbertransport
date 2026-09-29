package ru.sberbank.ditsib.transport.request.database.model.approvals.settings;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

/**
 * Сущность настроек согласования поездок на такси
 */
@Entity
@DiscriminatorValue(value = TransportTypeEnum.Constants.TAXI_STRING)
@NoArgsConstructor
@SuperBuilder
public class TaxiApprovalsSettings extends ApprovalsSettings {

}
