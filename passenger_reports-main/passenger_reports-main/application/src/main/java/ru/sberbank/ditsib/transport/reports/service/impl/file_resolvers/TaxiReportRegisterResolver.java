package ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.*;
import ru.sberbank.ditsib.transport.reports.dto.files.TaxiReportRegisterDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.TaxiResponseDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;
import ru.sberbank.ditsib.transport.reports.mappers.impl.StatusCodeHelper;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.service.RequestRegisterService;
import ru.sberbank.ditsib.transport.reports.service.TaxiTripService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.CONFIDENTIAL;
import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.WITHOUT_RATING;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaxiReportRegisterResolver implements DataExporter<TaxiReportRegisterDTO> {
    
    private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm:ss";
    private static final String DATE_PATTERN = "dd.MM.yyyy";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DURATION_FORMAT = "%02d:%02d:%02d";
    private static final String NOT_COOP = "Индивидуальная";
    private static final String COOP = "Совместная";
    private final RequestForXlsxMapper requestForXlsxMapper;
    private final RequestRegisterService requestRegisterService;
    private final TaxiTripService taxiTripService;
    
    @Override
    public String getCaption() {
        return CONFIDENTIAL;
    }
    
    @Override
    public List<TaxiReportRegisterDTO> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        log.debug("TaxiReportResolver: exportData start1");
        
        var filters = getFilters(parameters);
        var taxiResponseDTOs = requestRegisterService.findTaxiRequests(filters);
        var coopTaxiTripMap = new HashMap<UUID, CoopTaxiTrip>();
        var singleTaxiTripMap = new HashMap<UUID, SingleTaxiTrip>();
        var taxiTripIds = new HashSet<String>();
        fillTaxiTripMaps(taxiResponseDTOs, coopTaxiTripMap, singleTaxiTripMap, taxiTripIds);
        
        return taxiResponseDTOs
                .map(taxiResponseDTO -> mapToTaxiReportRegisterDTO(taxiResponseDTO, coopTaxiTripMap, singleTaxiTripMap))
                .toList();
    }
    
    private RequestForTaxiReportDTO getFilters(Map<String, ?> parameters) {
        var filters = requestForXlsxMapper.toTaxiDto(parameters);
        if (filters.getDesiredDateRange() == null
            && filters.getCreationDate() == null
            && filters.getActualDepartureDate() == null
            && filters.getRequestHumanId() == null) {
            filters.setCreationDate(requestForXlsxMapper.getCurrentYear());
        }
        
        var pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(Integer.MAX_VALUE);
        filters.setPageSetting(pageSetting);
        
        return filters;
    }
    
    private TaxiReportRegisterDTO mapToTaxiReportRegisterDTO(
            TaxiResponseDTO resp,
            HashMap<UUID,
                    CoopTaxiTrip> coopTaxiTripMap,
            HashMap<UUID, SingleTaxiTrip> singleTaxiTripMap
                                                            ) {
        
        TaxiTrip taxiTrip;
        if (resp.isCoopTrip()) {
            taxiTrip = coopTaxiTripMap.get(resp.getSharedRideId());
        } else {
            taxiTrip = singleTaxiTripMap.get(resp.getId());
        }
        
        var dto = new TaxiReportRegisterDTO();
        dto.setOrganizationOfficialName(resp.getOrganizationOfficialName());
        dto.setRequestId(resp.getHumanReadableId());
        dto.setMvz(resp.getCostCenter());
        dto.setCreationDateTime(convertDateTime(resp.getCreationTime(), resp.getTimeZone()));
        dto.setRequestStatus(Optional.ofNullable(resp.getStatus()).map(TripRequestStatus::getDescription).orElse(null));
        dto.setStatusCode(StatusCodeHelper.getStatusCodeNameForTaxi(resp.getStatusCode()));
        dto.setTripType(resp.isCoopTrip() ? COOP : NOT_COOP);
        dto.setTaxiClass(Optional.ofNullable(resp.getTaxiClass()).map(TaxiClass::getRusName).orElse(""));
        dto.setDepartureAddress(resp.getDepartureAddress());
        dto.setIntermediateAddresses(resp.getIntermediateAddresses());
        dto.setWaypointsCount(Optional.ofNullable(resp.getExpected()).map(ExpectedDataDTO::getWaypointsCount).orElse(0));
        dto.setWaypointWaitTime(getWaypointWaitTime(resp));
        dto.setDestinationAddress(resp.getDestinationAddress());
        // В качестве даты/времени выполнения заявки считаем дату/время прибытия водителя в точку отправления
        dto.setExecutionDateOfRequest(convertDateTime(resp.getDriverArrivedDatetime(), resp.getTimeZone()));
        dto.setFinishedTime(convertDateTime(resp.getFinishedTime(), resp.getTimeZone()));
        dto.setDesiredTime(getDesiredTime(resp));
        dto.setDesiredDate(getDesiredDate(resp));
        dto.setDeadline(convertDateTime(resp.getDeadline(), resp.getTimeZone()));
        dto.setDriverArrivedDatetime(convertDateTime(resp.getDriverArrivedDatetime(), resp.getTimeZone()));
        dto.setDeadlineViolation(resp.getDeadlineViolation());
        dto.setCustomerPersonnelNumber(getCustomerPersonnelNumber(resp));
        dto.setCustomerFio(getCustomerFio(resp));
        dto.setPassengerActivity(getPassengerActivity(resp));
        dto.setNumberPassengersJoined(Optional.ofNullable(resp.getNumberPassengersJoined()).orElse(0));
        dto.setPassengerCount(resp.getPassengerCount());
        dto.setTripPurpose(getPurpose(resp));
        dto.setLimitId(getLimitId(resp));
        dto.setCommentForDriver(resp.getCommentForDriver());
        dto.setDepartmentCode(Optional.ofNullable(resp.getDepartment()).map(DepartmentShortDTO::getCode).orElse(null));
        dto.setPassengerDepartment1(resp.getPassengerDepartment1());
        dto.setPassengerDepartment2(resp.getPassengerDepartment2());
        dto.setPassengerDepartment3(resp.getPassengerDepartment3());
        dto.setPassengerDepartment4(resp.getPassengerDepartment4());
        dto.setPassengerDepartment5(resp.getPassengerDepartment5());
        dto.setPassengerDepartment6(resp.getPassengerDepartment6());
        dto.setPassengerPosition(Optional.ofNullable(resp.getPassenger()).map(EmployeeDTO::getPositionName).orElse(null));
        dto.setPassengerPersonnelNumber(Optional.ofNullable(resp.getPassenger()).map(EmployeeDTO::getPersonnelNumber).orElse(null));
        dto.setPassengerFio(getPassengerFio(resp));
        dto.setApproverPersonnelNumber(getApproverPersonnelNumber(resp));
        dto.setApproverFio(getApproverFio(resp));
        dto.setTariffId(Optional.ofNullable(resp.getTariff()).map(TariffShortDTO::getHumanReadableId).orElse(null));
        dto.setPassengerMobilePhone(getPassengerMobilePhone(resp));
        dto.setWorkGroup(Optional.ofNullable(resp.getTariff()).map(TariffShortDTO::getWorkGroup).orElse(null));
        dto.setContractorName(Optional.ofNullable(resp.getContractor()).map(ContractorDTO::getName).orElse(null));
        dto.setRequestExpectedDistance(Optional.ofNullable(resp.getExpected()).map(ExpectedDataDTO::getDistance).orElse(null));
        dto.setTripFactDistance(Optional.ofNullable(taxiTrip).map(TaxiTrip::getTripFactDistance).orElse(null));
        dto.setRequestExpectedCost(Optional.ofNullable(resp.getExpected()).map(expected -> expected.getCost() / 100.0).orElse(null));
        dto.setTripFactPrice(Optional.ofNullable(taxiTrip).map(TaxiTrip::getTripFactPrice).map(price -> price / 100.0).orElse(null));
        dto.setRequestExpectedTime(Optional.ofNullable(resp.getExpected()).map(expected -> String.valueOf(expected.getTime())).orElse(null));
        dto.setTripFactDuration(getTripFactDuration(taxiTrip));
        dto.setActualWaitingTime(getActualWaitingTime(taxiTrip));
        dto.setSavingsCash(resp.getSavingsCash() != null ? (resp.getSavingsCash() / 100.0) : null);
        dto.setSavingsProcent(resp.getSavingsProcents());
        dto.setSharedRideOwner(Boolean.TRUE.equals(resp.getSharedRideOwner()) ? "Да" : "Нет");
        dto.setRating(Optional.ofNullable(resp.getRequestRating()).map(RequestRatingDTO::getRating).map(String::valueOf).orElse(WITHOUT_RATING));
        dto.setRatingComment(Optional.ofNullable(resp.getRequestRating()).map(RequestRatingDTO::getRatingComment).orElse(null));
        dto.setSharedRideId(Optional.ofNullable(resp.getSharedRideId()).map(UUID::toString).orElse(null));
        dto.setRequestClosedDatetime(convertDateTime(resp.getRequestClosedDatetime(), resp.getTimeZone()));
        dto.setJoinedPassengers(resp.getJoinedPassengers());
        dto.setSource(resp.getSource());
        dto.setMinTaxiTariffCost(resp.getMinTaxiTariffCost());
        dto.setTotalSharedRequestCount(resp.getTotalSharedRequestCount());
        dto.setLimitDebit(resp.getLimitDebit());
        dto.setDepartmentEconomy(resp.getDepartmentEconomy());
        dto.setFinancialImpact(resp.getFinancialImpact());
        dto.setFinancialImpactShared(resp.getFinancialImpactShared());
        dto.setFinancialImpactJoined(resp.getFinancialImpactJoined());
        dto.setCommentForPurpose(resp.getCommentForPurpose());
        dto.setRequestStatusCode(resp.getRequestStatusCode());
        dto.setDeadlineState(resp.getDeadlineViolation().equalsIgnoreCase("да") ? "ДА" : "НЕТ");
        dto.setSavings(Boolean.TRUE.equals(resp.getSavings()) ? "Есть" : "Нет");
        dto.setContractNumber(resp.getContractNumber());
        dto.setTripClass(resp.getTripClass());
        dto.setExecutorGroupId(String.valueOf(resp.getExecutorGroupId()));
        dto.setExecutorGroupName(resp.getExecutorGroupName());
        dto.setRegistryFactWaitingTime(resp.getRegistryFactWaitingTime());
        dto.setRegistryFactDistance(resp.getRegistryFactDistance());
        dto.setRegistryFactCost(resp.getRegistryFactCost());
        dto.setRegistryHumanReadableId(resp.getRegistryHumanReadableId());
        dto.setRegistryFactPayment(resp.getRegistryFactPayment());
        return dto;
    }
    
    private void fillTaxiTripMaps(
            Page<TaxiResponseDTO> requests,
            Map<UUID, CoopTaxiTrip> coopTaxiTripMap,
            Map<UUID, SingleTaxiTrip> singleTaxiTripMap,
            Set<String> taxiTripIds
                                 ) {
        
        var singleRequestIdList = requests.stream()
                                          .filter(r -> !r.isCoopTrip())
                                          .map(TaxiResponseDTO::getId)
                                          .toList();
        var singleTaxiTrips = taxiTripService.findAllByRequestIds(singleRequestIdList);
        singleTaxiTrips.forEach(trip -> {
            singleTaxiTripMap.put(trip.getRequest().getId(), trip);
            taxiTripIds.add(trip.getTaxiId());
        });
        
        var rideIdList = requests.stream()
                                 .filter(TaxiResponseDTO::isCoopTrip)
                                 .map(TaxiResponseDTO::getSharedRideId)
                                 .toList();
        var coopTaxiTrips = taxiTripService.findAllBySharedRideIds(rideIdList);
        coopTaxiTrips.forEach(trip -> {
            coopTaxiTripMap.put(trip.getRideId(), trip);
            taxiTripIds.add(trip.getTaxiId());
        });
    }
    
    private String convertDateTime(LocalDateTime dateTime, String timeZone) {
        if (dateTime != null) {
            ZonedDateTime zdt = dateTime.atZone(ZoneOffset.UTC);
            zdt = zdt.withZoneSameInstant(ZoneId.of(timeZone == null ? "UTC" : timeZone));
            return zdt.format(DateTimeFormatter.ofPattern(TaxiReportRegisterResolver.DATE_TIME_PATTERN));
        } else {
            return null;
        }
    }
    
    private String getPassengerFio(TaxiResponseDTO r) {
        if (r.getPassenger() != null) {
            return r.getPassenger().getLastName() + " " +
                   r.getPassenger().getFirstName() +
                   (r.getPassenger().getPatronymic() == null ? "" : (" " + r.getPassenger().getPatronymic()));
        } else {
            return null;
        }
    }
    
    private String getPassengerActivity(TaxiResponseDTO r) {
        if (r.getPassenger() != null && r.getPassenger().getItinerantType() != null) {
            return "Да";
        } else {
            return "Нет";
        }
    }
    
    private String getPurpose(TaxiResponseDTO r) {
        if (r.getPurpose() != null && r.getPurpose().getPurpose() != null) {
            return r.getPurpose().getPurpose();
        } else {
            return null;
        }
    }
    
    private String getDesiredDate(TaxiResponseDTO r) {
        if (r.getDesiredDate() != null) {
            ZonedDateTime zdt = r.getDesiredDate().atZone(ZoneId.of("UTC"));
            zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
            return zdt.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        } else {
            return null;
        }
    }
    
    private String getDesiredTime(TaxiResponseDTO r) {
        if (r.getDesiredDate() != null) {
            ZonedDateTime zdt = r.getDesiredDate().atZone(ZoneId.of("UTC"));
            zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
            return zdt.format(DateTimeFormatter.ofPattern(TIME_PATTERN));
        } else {
            return null;
        }
    }
    
    private String getLimitId(TaxiResponseDTO r) {
        var lim = r.getLimit();
        if (lim != null) {
            return lim.getHumanReadableId();
        } else {
            return null;
        }
    }
    
    private String getCustomerPersonnelNumber(TaxiResponseDTO r) {
        if (r.getAuthor() != null && r.getAuthor().getPersonnelNumber() != null) {
            return r.getAuthor().getPersonnelNumber();
        } else {
            return null;
        }
    }
    
    private String getCustomerFio(TaxiResponseDTO r) {
        if (r.getAuthor() != null) {
            return r.getAuthor().getLastName() + " " + r.getAuthor().getFirstName() + " " + r.getAuthor().getPatronymic();
        } else {
            return null;
        }
    }
    
    private String getApproverPersonnelNumber(TaxiResponseDTO r) {
        if (r.getApprovedBy() != null && r.getApprovedBy().getPersonnelNumber() != null) {
            return r.getApprovedBy().getPersonnelNumber();
        } else {
            return null;
        }
    }
    
    private String getApproverFio(TaxiResponseDTO r) {
        if (r.getApprovedBy() != null) {
            return r.getApprovedBy().getLastName() + " " + r.getApprovedBy().getFirstName() + " " + r.getApprovedBy().getPatronymic();
        } else {
            return null;
        }
    }
    
    private String getPassengerMobilePhone(TaxiResponseDTO r) {
        if (r.getPassenger() != null
            && r.getPassenger().getPhone() != null
            && !r.getPassenger().getPhone().isBlank()) {
            return r.getPassenger().getPhone();
        } else {
            return null;
        }
    }
    
    private String getTripFactDuration(TaxiTrip tt) {
        if (tt != null && tt.getTripFactDuration() != null) {
            Duration factDuration = tt.getTripFactDuration();
            return String.format(DURATION_FORMAT, factDuration.toHours(), factDuration.toMinutesPart(), factDuration.toSecondsPart());
        }
        return null;
    }
    
    private String getWaypointWaitTime(TaxiResponseDTO r) {
        Duration resultWaitTime = Duration.ZERO;
        if (r.getExpected().getWaypoints().size() > 2) {
            for (int i = 1; i < r.getExpected().getWaypoints().size() - 1; i++) {
                resultWaitTime = resultWaitTime.plus(r.getExpected().getWaypoints().get(i).getWaitTime(), ChronoUnit.MINUTES);
            }
        }
        return String.format(DURATION_FORMAT, resultWaitTime.toHours(), resultWaitTime.toMinutesPart(), resultWaitTime.toSecondsPart());
    }
    
    private String getActualWaitingTime(TaxiTrip tr) {
        if (tr != null && tr.getTripFactWaitTime() != null) {
            var t = tr.getTripFactWaitTime();
            return String.format(DURATION_FORMAT, t.toHours(), t.toMinutesPart(), t.toSecondsPart());
        }
        return null;
    }
}
