package ru.sber.transport.push.messaging.listeners;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.push.business.PushService;
import ru.sber.transport.text.TextService;
import ru.sber.transport.messaging.messages.PushMessage;

import java.util.function.Consumer;

@Configuration
public class ListenerConfig {

    @Bean
    Consumer<Message<PushMessage>> pushInput(TextService textService, PushService service) {
        return pushInputSsl(textService, service);
    }

    @Bean
    Consumer<Message<PushMessage>> pushInputSsl(TextService textService, PushService service) {
        return message -> {
            var payload = message.getPayload();
            var data = payload.getData();
            if (data == null || data.isEmpty()) {
                data = payload.getAdditionalData();
            }
            var text = textService.createFromTemplate(payload.getTemplate(), data);
            service.send(payload.getId(), payload.getReceivers(), payload.getType(), text, payload.getAdditionalData());
        };
    }

    @Bean
    Consumer<Message<ru.sber.transport.messages.push.avro.PushMessage>> pushInputAvro(TextService textService, PushService service) {
        return message -> {
            var payload = message.getPayload();
            var data = payload.getData();
            if (data.isEmpty()) {
                data = payload.getAdditionalData();
            }
            var text = textService.createFromTemplate(payload.getTemplate(), data);
            service.send(payload.getId(), payload.getReceivers(), payload.getType(), text, payload.getAdditionalData());
        };
    }

}
