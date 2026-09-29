package ru.sber.transport.audit.fakeApp;

import org.springframework.stereotype.Component;
import ru.sber.transport.audit.annotation.Audited;

@Component
public class AuditedAction {

    @Audited(source = "source1", value = "Check success action")
    public void action() {
    }

    @Audited(source = "source2", value = "Check failed action")
    public void failed_action() {
        throw new RuntimeException();
    }

    @Audited(source = "source3", value = "Check success action with data '{data}'")
    public String success_with_data(String data) {
        return "Result " + data;
    }

    @Audited(source = "source4", value = "Check fail action with data '{data}'")
    public String fail_with_data(String data) {
        throw new RuntimeException();
    }

}
