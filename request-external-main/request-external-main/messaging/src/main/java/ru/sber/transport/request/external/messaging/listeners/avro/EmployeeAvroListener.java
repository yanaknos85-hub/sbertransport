package ru.sber.transport.request.external.messaging.listeners.avro;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.messages.corporate.avro.EmployeeMessage;
import ru.sber.transport.request.external.messaging.listeners.model.avro.EmployeeAvroData;

@RequiredArgsConstructor
public class EmployeeAvroListener implements Consumer<Message<EmployeeMessage>> {

    private final EmployeesProvider employeesProvider;

    @Override
    public void accept(Message<EmployeeMessage> raw) {
        employeesProvider.save(new EmployeeAvroData(raw.getPayload()));
    }
}
