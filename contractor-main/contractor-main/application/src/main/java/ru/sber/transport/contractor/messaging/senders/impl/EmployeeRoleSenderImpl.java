package ru.sber.transport.contractor.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.contractor.messages.EmployeeRoleMessage;
import ru.sber.transport.contractor.messaging.senders.EmployeeRoleSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class EmployeeRoleSenderImpl implements EmployeeRoleSender {

    @Qualifier("employeeRoleOutput")
    private final ObjectProvider<OutputBridge> employeeRoleOutput;

    @Qualifier("employeeRoleOutputSsl")
    private final ObjectProvider<OutputBridge> employeeRoleOutputSsl;

    @Override
    public void send(UUID employeeId, String role) {
        var message = new EmployeeRoleMessage(employeeId, role);
        employeeRoleOutput.ifAvailable(ob -> ob.send(message));
        employeeRoleOutputSsl.ifAvailable(ob -> ob.send(message));
    }
}
