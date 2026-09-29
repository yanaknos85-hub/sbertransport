package ru.sber.transport.dispatcher.dto.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DispatcherSearchParameters implements SortField {

    FIRST_NAME("firstName"),

    LAST_NAME("lastName"),

    PATRONYMIC("patronymic"),

    ACTIVE("active"),

    AUTOPARK_ID("autoparkId");

    private final String name;
}
