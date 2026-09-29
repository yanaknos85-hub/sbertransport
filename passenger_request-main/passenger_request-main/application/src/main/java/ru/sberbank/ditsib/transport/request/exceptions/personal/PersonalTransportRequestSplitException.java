package ru.sberbank.ditsib.transport.request.exceptions.personal;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PersonalTransportRequestSplitException extends RuntimeException {
    private final UUID id;
    private final String humanReadableId;
    private final String status;

    public PersonalTransportRequestSplitException(String message, UUID id, String humanReadableId, String status) {
        super(message);
        this.id = id;
        this.humanReadableId = humanReadableId;
        this.status = status;
    }
}
