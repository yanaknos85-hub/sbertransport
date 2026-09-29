package ru.sber.transport.trips.cargo.web.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.trips.cargo.business.dto.TripAssignStatisticDto;
import ru.sber.transport.trips.cargo.web.controller.StatisticController;
import ru.sber.transport.trips.cargo.web.service.StatisticService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatisticControllerImpl implements StatisticController {

    private final StatisticService statisticService;

    @CheckOrganizationAccess
    @Override
    public TripAssignStatisticDto getAssignStatistic(@Organization UUID contractorId, UUID autoparkId) {
        return statisticService.getAssignStatistic(contractorId, autoparkId);
    }
}
