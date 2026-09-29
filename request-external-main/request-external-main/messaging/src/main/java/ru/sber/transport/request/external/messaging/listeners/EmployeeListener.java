package ru.sber.transport.request.external.messaging.listeners;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.request.external.messaging.listeners.model.EmployeeData;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

@RequiredArgsConstructor
public class EmployeeListener implements Consumer<Message<EmployeeMessage>> {

    private final EmployeesProvider employeesProvider;

    @Override
    public void accept(Message<EmployeeMessage> raw) {
        employeesProvider.save(new EmployeeData(raw.getPayload()));
    }
}
