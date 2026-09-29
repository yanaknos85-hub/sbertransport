package ru.sberbank.ditsib.transport.request.database.model.deadline;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.LimitType;

/** Сущность - настройка контрольного срока для конкретного типа лимита */
@Entity
@DiscriminatorValue(value = "LIMIT_ITEM")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LimitDeadlineSettingsItem extends DeadlineSettingsItem {
    
    /** Соответствующий тип лимита, для которого создана настройка */
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type")
    private LimitType limitType;
}
