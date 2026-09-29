package ru.sber.transport.telemechanic.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DispatcherSortOption {
    
    PERSONNEL_NUMBER("Табельный номер"),
    ORGANIZATION_NAME("Наименование организации");
    
    private final String description;
}
