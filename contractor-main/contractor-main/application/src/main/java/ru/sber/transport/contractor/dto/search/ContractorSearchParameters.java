package ru.sber.transport.contractor.dto.search;

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
    RATING("rating"),
    SERVICE_TYPE("serviceType"),
    PERSON_FIRST_NAME("personFirstName"),
    PERSON_LAST_NAME("personLastName"),
    PERSON_PATRONYMIC("personPatronymic");

    private final String name;

}
