package ru.sber.transport.trips.cargo.web.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.trips.cargo.business.dto.TripAssignStatisticDto;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.web.service.StatisticService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class StatisticServiceImpl implements StatisticService {

    private final TripProvider tripProvider;

    @Override
    public TripAssignStatisticDto getAssignStatistic(UUID contractorId, UUID autoparkId) {
        var assignCountStatuses = List.of(TripStatus.DRIVER_ASSIGNED, TripStatus.DRIVER_ON_THE_WAY,
                TripStatus.DRIVER_ARRIVED, TripStatus.TRIP_IN_PROGRESS, TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED);
        var noAssignCountStatuses = List.of(TripStatus.SENT_TO_CONTRACTOR, TripStatus.WAITING_FOR_ASSIGNMENT);

        var assignCount = tripProvider.countByContractorIdAndStatusIn(contractorId, autoparkId, assignCountStatuses);
        var notAssignCount = tripProvider.countByContractorIdAndStatusIn(contractorId, autoparkId, noAssignCountStatuses);
        var totalCount = assignCount + notAssignCount;

        return new TripAssignStatisticDto(totalCount, assignCount, notAssignCount);
    }
}
