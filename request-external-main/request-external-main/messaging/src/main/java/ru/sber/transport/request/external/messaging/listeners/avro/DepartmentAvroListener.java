package ru.sber.transport.request.external.messaging.listeners.avro;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.messages.corporate.avro.DepartmentMessage;
import ru.sber.transport.request.external.messaging.listeners.model.avro.DepartmentAvroData;

@RequiredArgsConstructor
@Slf4j
public class DepartmentAvroListener implements Consumer<Message<DepartmentMessage>> {

    private final DepartmentsProvider departmentsProvider;

    @Override
    public void accept(Message<DepartmentMessage> raw) {
        log.info("Received department message: {}", raw);
        departmentsProvider.save(new DepartmentAvroData(raw.getPayload()));
    }
}
