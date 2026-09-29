package ru.sber.transport.telemechanic.dto.transport;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TransportSearchParameters implements SortField {
    
    STATE_NUMBER("stateNumber");
    
    private final String name;

}
