package ru.sber.transport.dispatcher.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.messaging.senders.ConfirmationSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.user_data_confirmation.message.UserDataConfirmationMessage;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConfirmationSenderImpl implements ConfirmationSender {

    @Qualifier("confirmationDataOutput")
    private final ObjectProvider<OutputBridge> confirmationDataOutput;

    @Qualifier("confirmationDataOutputSsl")
    private final ObjectProvider<OutputBridge> confirmationDataOutputSsl;

    @Override
    public void send(UUID id, String phone) {
        var message = new UserDataConfirmationMessage(id, phone);
        confirmationDataOutput.ifAvailable(ob -> ob.send(message));
        confirmationDataOutputSsl.ifAvailable(ob -> ob.send(message));
    }
}
