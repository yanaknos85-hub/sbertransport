package ru.sber.transport.dispatcher.dto.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ContractorSearchParameters implements SortField {

    MSRN("msrn"),
    NAME("name"),
    TIN("tin"),
    RATING_FROM("ratingFrom"),
    RATING_TO("ratingTo"),
    REGIONS("regionIds"),
    RATING("rating"),
    IS_INTERNAL("isInternal");

    private final String name;

}
