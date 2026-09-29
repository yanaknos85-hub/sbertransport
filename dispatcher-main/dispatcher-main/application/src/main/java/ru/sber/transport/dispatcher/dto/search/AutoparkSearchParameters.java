package ru.sber.transport.dispatcher.dto.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AutoparkSearchParameters implements SortField {

    NAME("name"),

    ACTIVE("active"),

    ROUTING_ID("routingId");

    private final String name;

}
