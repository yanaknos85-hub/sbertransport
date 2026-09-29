package ru.sberbank.ditsib.transport.request.database.model.approvals.settings;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

/**
 * Сущность настроек согласования поездок на общественном траспорте
 */
@Entity
@Getter
@Setter
@DiscriminatorValue(TransportTypeEnum.Constants.PUBLIC_STRING)
@NoArgsConstructor
@SuperBuilder
public class PublicTrApprovalsSettings extends ApprovalsSettings {
    
    /** Необходимость прикрепления документа на этапе "Создание" (для междугородних поездок) */
    @Column(name = "approval_document_check")
    private boolean approvalDocumentCheck;
    
    /** Необходимость этапа утверждения */
    @Column(name = "affirmative_active")
    private boolean affirmativeActive;
    
    /** Необходимость этапа подтверждения */
    @Column(name = "trip_confirmation_active")
    private boolean tripConfirmationActive;
    
    /** Необходимость прикрепления документа на этапе "Подтверждение поездки" (для междугородних поездок)  */
    @Column(name = "trip_confirmation_document_check")
    private boolean tripConfirmationDocumentCheck;
}
