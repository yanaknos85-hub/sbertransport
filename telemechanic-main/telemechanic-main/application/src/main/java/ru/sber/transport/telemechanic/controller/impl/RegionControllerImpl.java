package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.RegionController;
import ru.sber.transport.telemechanic.dto.RegionDto;
import ru.sber.transport.telemechanic.service.RegionService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;

import java.util.List;

@RestController
@E2EController
@RequiredArgsConstructor
public class RegionControllerImpl implements RegionController {
    
    private final RegionService regionService;
    
    @Override
    public List<RegionDto> getRegions() {
        return regionService.getRegionList();
    }
}
