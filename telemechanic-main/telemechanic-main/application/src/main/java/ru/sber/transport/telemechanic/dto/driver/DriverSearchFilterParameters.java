package ru.sber.transport.telemechanic.dto.driver;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sberbank.ditsib.request.SortField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DriverSearchFilterParameters implements SortField {
    
    SEARCH_TEXT ("searchText"),
    
    TRANSPORT_ID("transportId");
    
    private final String name;
    
}
