package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.dto.IVisibilityDto;

public interface DefaultResolver<T extends IVisibilityDto> {
    
    T getDefault();
    
}
