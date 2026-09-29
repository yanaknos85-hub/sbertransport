package ru.sber.transport.dispatcher.dto.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor
public enum ShiftConflictParameters implements SortField {

    PERSONNEL_NUMBER("personnelNumber"),
    STATE_NUMBER("stateNumber"),
    CONFLICT_REASON("conflictReason");

    private final String name;

}
