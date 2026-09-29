package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.request.model.StatsDTO;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.RequestRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.dto.RatingsDTO;
import ru.sberbank.ditsib.transport.request.messaging.senders.StatsSender;
import ru.sberbank.ditsib.transport.request.service.StatsService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Имплементация сервиса для работы с маджента
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class StatsServiceImpl implements StatsService {
    
    private final OrganizationService organizationService;
    
    private final StatsSender statsSender;
    
    private final RequestRepository requestRepository;
    
    @Override
    public List<StatsDTO> processStats() {
        
        log.info("processStats: start");
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int prevYear = currentYear;
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int prevMonth = currentMonth - 1;
        if (prevMonth == 0) {
            prevMonth = 12;
            prevYear = currentYear - 1;
        }
        List<StatsDTO> listStats = getStatsForMonth(currentMonth, currentYear);
        listStats.addAll(getStatsForMonth(prevMonth, prevYear));
        sendStats(listStats);
        log.info("processStats: end: " + listStats.size());
        return listStats;
    }
    
    @Override
    public List<StatsDTO> processStatsForMonthAndYear(int month, int year) {
        try {
            log.info("processStatsForMonthAndYear: start");
            List<StatsDTO> listStats = getStatsForMonth(month, year);
            sendStats(listStats);
            log.info("processStatsForMonthAndYear: end: " + listStats);
            return listStats;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    private void sendStats(List<StatsDTO> listStats) {
        for (StatsDTO statsDTO : listStats) {
            statsSender.send(statsDTO);
        }
    }
    
    private List<StatsDTO> getStatsForMonth(Integer month, Integer year) {
        List<StatsDTO> list = new ArrayList<>();
        List<Organization> organizations = organizationService.getAll();
        for (Organization organization : organizations) {
            long countAll = requestRepository.countAll(organization.getId(), year, month);
            if (countAll > 0) {
                for (TransportTypeEnum transportType : TransportTypeEnum.getByServiceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)) {
                    StatsDTO statsDTO = getStatsForParameters(organization, transportType, month, year);
                    long total = statsDTO.getTotalExecuted() + statsDTO.getTotalNotExecuted() + statsDTO.getTotalCanceled();
                    if (total > 0) {
                        log.info("getStatsForMonth: get for org " + organization.getId() + ", "
                                 + month + "-" + year + ", "
                                 + transportType + ": " + total);
                        list.add(statsDTO);
                    }
                }
            }
        }
        return list;
    }
    
    private StatsDTO getStatsForParameters(
            Organization organization,
            TransportTypeEnum transportType,
            Integer month, Integer year
                                          ) {
        List<TripRequestStatus> executedStatuses = Arrays.asList(TripRequestStatus.TAXI_TRIP_FINISHED,
                                                                 TripRequestStatus.PERSONAL_PAYMENT_DONE,
                                                                 TripRequestStatus.PUBLIC_PAYMENT_DONE,
                                                                 TripRequestStatus.CARSHARING_TRIP_FINISHED);
        List<TripRequestStatus> cancelStatuses = Arrays.asList(TripRequestStatus.TAXI_CANCELLED);
        
        long valueTotal = requestRepository.countAllByYearAndMonthAndOrganizationIdAndTransportType(
                year, month, organization.getId(), transportType);
        
        long valueExecuted = requestRepository.countAllByYearAndMonthAndOrganizationIdAndTransportTypeAndStatuses(
                year, month, organization.getId(), transportType, executedStatuses);
        
        long valueCanceled = requestRepository.countAllByYearAndMonthAndOrganizationIdAndTransportTypeAndStatuses(
                year, month, organization.getId(), transportType, cancelStatuses);
        
        Double valueSum1 = requestRepository.sumAllByYearAndMonthAndOrganizationIdAndTransportType(
                year, month, organization.getId(), transportType);
        long valueSum = Math.round(valueSum1 == null ? 0 : valueSum1);
        long valueNotExecuted = valueTotal - valueExecuted - valueCanceled;
        
        final Map<Integer, Long> starsMap = requestRepository.countAllPerStarsByYearAndMonthAndOrganizationIdAndTransportType(
                year, month, organization.getId(), transportType).stream()
                .collect(Collectors.toMap(RatingsDTO::numberOfStars, RatingsDTO::numberOfRatings));
        IntStream.range(1, 6).filter(value -> starsMap.get(value) == null).forEach(value -> starsMap.put(value, 0L));
        long csiPositive = starsMap.get(4) + starsMap.get(5);
        long csiNegative = starsMap.get(1) + starsMap.get(2) + starsMap.get(3);
        
        TripRequestStatus cancelStatus = TripRequestStatus.TAXI_CANCELLED;
        List<Integer> statusCodes = List.of(TripRequestStatus.TaxiStatusCode.TAXI_CANCELLED_BY_DRIVER.getCode());
        long slaTotal = requestRepository.countAllByYearAndMonthAndOrganizationIdAndTransportTypeAndStatusesOrCancelStatusAndStatusCodes(
                year, month, organization.getId(), transportType, executedStatuses, cancelStatus, statusCodes);
        
        long slaWithViolation = 0;
        if (transportType.equals(TransportTypeEnum.TAXI)) {
            slaWithViolation =
                    requestRepository.countWithViolationTaxi(year, month, organization.getId(), TransportTypeEnum.TAXI,
                                                             executedStatuses, DeadlineState.RED, cancelStatus, statusCodes);
        }
        if (transportType.equals(TransportTypeEnum.PERSONAL)) {
            slaWithViolation =
                    requestRepository.countWithViolationPersonal(year, month, organization.getId(), TransportTypeEnum.PERSONAL,
                                                                 executedStatuses);
        }
        if (transportType.equals(TransportTypeEnum.PUBLIC)) {
            slaWithViolation =
                    requestRepository.countWithViolationPublic(year, month, organization.getId(), TransportTypeEnum.PUBLIC,
                                                               executedStatuses);
        }
        
        long slaWithoutViolation = slaTotal - slaWithViolation;
        
        StatsDTO statsDTO = new StatsDTO();
        statsDTO.setOrganizationId(organization.getId());
        statsDTO.setYear(year);
        statsDTO.setMonth(month);
        statsDTO.setServiceType(transportType.getServiceType());
        statsDTO.setTransportType(transportType);
        
        statsDTO.setTotalExecuted(valueExecuted);
        statsDTO.setTotalNotExecuted(valueNotExecuted);
        statsDTO.setTotalSum(valueSum);
        statsDTO.setTotalCanceled(valueCanceled);
        
        statsDTO.setCsiStarPositive(csiPositive);
        statsDTO.setCsiStarNegative(csiNegative);
        
        statsDTO.setSlaWithoutViolation(slaWithoutViolation);
        statsDTO.setSlaWithViolation(slaWithViolation);
        
        return statsDTO;
    }
}
