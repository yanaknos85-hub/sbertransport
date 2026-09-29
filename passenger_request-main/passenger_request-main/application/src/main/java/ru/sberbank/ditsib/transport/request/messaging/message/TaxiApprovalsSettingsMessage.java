package ru.sberbank.ditsib.transport.request.messaging.message;

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
public class TaxiApprovalsSettingsMessage extends ApprovalsSettingsMessage {
}
