package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.request.messaging.message.ApprovalsSettingsMessage;

/**
 * Сообщение с настройками согласований
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicApprovalsSettingsMessage extends ApprovalsSettingsMessage {
    
    /** Необходимость прикрепления документа на этапе "Создание" (OT) */
    private boolean approvalDocumentCheck;
    
    /** Необходимость этапа утверждения (OT) */
    private boolean affirmativeActive;
    
    /** Необходимость этапа подтверждения (OT) */
    private boolean tripConfirmationActive;
    
    /** Необходимость проверки документа на этапе "Подтверждение поездки" (OT) */
    private boolean tripConfirmationDocumentCheck;
}
