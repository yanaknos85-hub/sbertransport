package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.dto.RegionDto;

import java.util.List;

public interface RegionService {
    
    List<RegionDto> getRegionList();
}
