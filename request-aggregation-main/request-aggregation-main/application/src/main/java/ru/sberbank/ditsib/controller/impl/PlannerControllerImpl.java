package ru.sberbank.ditsib.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.controller.PlannerController;
import ru.sberbank.ditsib.dto.AggregatedMainLeadDto;
import ru.sberbank.ditsib.service.MainLeadService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlannerControllerImpl implements PlannerController {

    private final MainLeadService mainLeadService;

    @Override
    public List<AggregatedMainLeadDto> getAllRequests() {
        return mainLeadService.getAllRequests();
    }
} 