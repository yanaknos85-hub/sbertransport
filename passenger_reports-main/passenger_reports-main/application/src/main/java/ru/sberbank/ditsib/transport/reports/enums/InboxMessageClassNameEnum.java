package ru.sberbank.ditsib.transport.reports.enums;

import lombok.Getter;
import ru.sber.transport.request.messaging.RequestMessage;

@Getter
public enum InboxMessageClassNameEnum {
    REQUEST_MESSAGE(RequestMessage.class.getName());
    
    private String value;
    
    InboxMessageClassNameEnum(String value) {
        this.value = value;
    }
}
