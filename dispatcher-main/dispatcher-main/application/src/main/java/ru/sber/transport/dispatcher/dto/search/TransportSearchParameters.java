package ru.sber.transport.dispatcher.dto.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TransportSearchParameters implements SortField {

    BRAND("brand"),
    MODEL("model"),
    STATE_NUMBER("stateNumber");

    private final String name;
}
