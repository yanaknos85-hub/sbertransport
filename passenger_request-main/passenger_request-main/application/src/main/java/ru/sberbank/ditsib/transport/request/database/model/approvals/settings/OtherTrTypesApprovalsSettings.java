package ru.sberbank.ditsib.transport.request.database.model.approvals.settings;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Сущность настроек согласования поездок для всех типов транспорта, кроме такси и общественного
 */
@Entity
@Getter
@Setter
@DiscriminatorValue("OTHER")
@NoArgsConstructor
@SuperBuilder
public class OtherTrTypesApprovalsSettings extends ApprovalsSettings {
    
    /** Необходимость этапа утверждения поездки */
    @Column(name = "trip_approval_active")
    private boolean tripApprovalActive;
}
