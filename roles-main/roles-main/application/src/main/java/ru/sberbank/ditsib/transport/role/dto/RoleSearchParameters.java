package ru.sberbank.ditsib.transport.role.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

/**
 * Parameters for sorting.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RoleSearchParameters implements SortField {

    /**
     * Code of role.
     */
    CODE("code"),

    /**
     * Name of role.
     */
    NAME("name")
    ;

    /**
     * Name of role.
     */
    private final String name;
}
