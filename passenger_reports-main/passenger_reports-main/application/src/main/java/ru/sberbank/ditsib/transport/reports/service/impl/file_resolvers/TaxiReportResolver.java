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
import ru.sberbank.ditsib.transport.reports.dto.DepartmentShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.ExpectedDataDTO;
import ru.sberbank.ditsib.transport.reports.dto.RequestRatingDTO;
import ru.sberbank.ditsib.transport.reports.dto.TariffShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.files.TaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.TaxiResponseDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;
import ru.sberbank.ditsib.transport.reports.mappers.impl.StatusCodeHelper;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.service.RequestService;
import ru.sberbank.ditsib.transport.reports.service.TaxiTripService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.CONFIDENTIAL;
import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.WITHOUT_RATING;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaxiReportResolver implements DataExporter<TaxiReportDTO> {
    
    private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm:ss";
    private static final String DATE_PATTERN = "dd.MM.yyyy";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DURATION_FORMAT = "%02d:%02d:%02d";
    private static final String NOT_COOP = "Индивидуальная";
    private static final String COOP = "Совместная";
    private final RequestForXlsxMapper requestForXlsxMapper;
    private final RequestService requestService;
    private final TaxiTripService taxiTripService;

    @Override
    public String getCaption() {
        return CONFIDENTIAL;
    }

    @Override
    public List<TaxiReportDTO> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        log.debug("TaxiReportResolver: exportData start1");
        var filters = getFilters(parameters);
        var taxiResponseDTOs = requestService.findTaxiRequests(filters);

        var coopTaxiTripMap = new HashMap<UUID, CoopTaxiTrip>();
        var singleTaxiTripMap = new HashMap<UUID, SingleTaxiTrip>();
        var taxiTripIds = new HashSet<String>();
        fillTaxiTripMaps(taxiResponseDTOs, coopTaxiTripMap, singleTaxiTripMap, taxiTripIds);

        return taxiResponseDTOs
                .map(taxiResponseDTO -> mapToTaxiReportDTO(taxiResponseDTO, coopTaxiTripMap, singleTaxiTripMap))
                .toList();
    }

    private TaxiReportDTO mapToTaxiReportDTO(TaxiResponseDTO taxiResponseDTO, HashMap<UUID,
            CoopTaxiTrip> coopTaxiTripMap, HashMap<UUID, SingleTaxiTrip> singleTaxiTripMap) {
        TaxiTrip taxiTrip;
        if (taxiResponseDTO.isCoopTrip()) {
            taxiTrip = coopTaxiTripMap.get(taxiResponseDTO.getSharedRideId());
        } else {
            taxiTrip = singleTaxiTripMap.get(taxiResponseDTO.getId());
        }

        var dto = new TaxiReportDTO();
        dto.setOrganizationOfficialName(taxiResponseDTO.getOrganizationOfficialName());
        dto.setRequestId(taxiResponseDTO.getHumanReadableId());
        dto.setMvz(taxiResponseDTO.getCostCenter());
        dto.setCreationDateTime(convertDateTime(taxiResponseDTO.getCreationTime(), taxiResponseDTO.getTimeZone()));
        dto.setRequestStatus(Optional.ofNullable(taxiResponseDTO.getStatus()).map(TripRequestStatus::getDescription).orElse(null));
        dto.setStatusCode(StatusCodeHelper.getStatusCodeNameForTaxi(taxiResponseDTO.getStatusCode()));
        dto.setTripType(taxiResponseDTO.isCoopTrip() ? COOP : NOT_COOP);
        dto.setTaxiClass(Optional.ofNullable(taxiResponseDTO.getTaxiClass()).map(TaxiClass::getRusName).orElse(""));
        dto.setDepartureAddress(taxiResponseDTO.getDepartureAddress());
        dto.setIntermediateAddresses(taxiResponseDTO.getIntermediateAddresses());
        dto.setWaypointsCount(Optional.ofNullable(taxiResponseDTO.getExpected()).map(ExpectedDataDTO::getWaypointsCount).orElse(0));
        dto.setWaypointWaitTime(getWaypointWaitTime(taxiResponseDTO));
        dto.setDestinationAddress(taxiResponseDTO.getDestinationAddress());

        // В качестве даты/времени выполнения заявки считаем дату/время прибытия водителя в точку отправления
        dto.setExecutionDateOfRequest(convertDateTime(taxiResponseDTO.getDriverArrivedDatetime(), taxiResponseDTO.getTimeZone()));

        dto.setFinishedTime(convertDateTime(taxiResponseDTO.getFinishedTime(), taxiResponseDTO.getTimeZone()));
        dto.setDesiredTime(getDesiredTime(taxiResponseDTO));
        dto.setDesiredDate(getDesiredDate(taxiResponseDTO));
        dto.setDeadline(convertDateTime(taxiResponseDTO.getDeadline(), taxiResponseDTO.getTimeZone()));
        dto.setDriverArrivedDatetime(convertDateTime(taxiResponseDTO.getDriverArrivedDatetime(), taxiResponseDTO.getTimeZone()));
        dto.setDeadlineViolation(taxiResponseDTO.getDeadlineViolation());
        dto.setCustomerPersonnelNumber(getCustomerPersonnelNumber(taxiResponseDTO));
        dto.setCustomerFio(getCustomerFio(taxiResponseDTO));
        dto.setPassengerActivity(getPassengerActivity(taxiResponseDTO));
        dto.setNumberPassengersJoined(Optional.ofNullable(taxiResponseDTO.getNumberPassengersJoined()).orElse(0));
        dto.setPassengerCount(taxiResponseDTO.getPassengerCount());
        dto.setTripPurpose(getPurpose(taxiResponseDTO));
        dto.setLimitId(getLimitId(taxiResponseDTO));
        dto.setCommentForDriver(taxiResponseDTO.getCommentForDriver());
        dto.setDepartmentCode(Optional.ofNullable(taxiResponseDTO.getDepartment()).map(DepartmentShortDTO::getCode).orElse(null));
        dto.setPassengerDepartment1(taxiResponseDTO.getPassengerDepartment1());
        dto.setPassengerDepartment2(taxiResponseDTO.getPassengerDepartment2());
        dto.setPassengerDepartment3(taxiResponseDTO.getPassengerDepartment3());
        dto.setPassengerDepartment4(taxiResponseDTO.getPassengerDepartment4());
        dto.setPassengerDepartment5(taxiResponseDTO.getPassengerDepartment5());
        dto.setPassengerDepartment6(taxiResponseDTO.getPassengerDepartment6());
        dto.setPassengerPosition(getPassengerPosition(taxiResponseDTO));
        dto.setPassengerPersonnelNumber(getPassengerPersonnelNumber(taxiResponseDTO));
        dto.setPassengerFio(getPassengerFio(taxiResponseDTO));
        dto.setApproverPersonnelNumber(getApproverPersonnelNumber(taxiResponseDTO));
        dto.setApproverFio(getApproverFio(taxiResponseDTO));
        dto.setTariffId(Optional.ofNullable(taxiResponseDTO.getTariff()).map(TariffShortDTO::getHumanReadableId).orElse(null));
        dto.setPassengerMobilePhone(getPassengerMobilePhone(taxiResponseDTO));
        dto.setWorkGroup(Optional.ofNullable(taxiResponseDTO.getTariff()).map(TariffShortDTO::getWorkGroup).orElse(null));
        dto.setContractorName(getContractorName(taxiResponseDTO));
        dto.setRequestExpectedDistance(getRequestExpectedDistance(taxiResponseDTO));
        dto.setTripFactDistance(getTripFactDistance(taxiTrip));
        dto.setRequestExpectedCost(getCost(taxiResponseDTO));
        dto.setTripFactPrice(getTripFactPrice(taxiTrip));
        dto.setRequestExpectedTime(getRequestExpectedTime(taxiResponseDTO));
        dto.setTripFactDuration(getTripFactDuration(taxiTrip));
        dto.setActualWaitingTime(getActualWaitingTime(taxiTrip));
        dto.setSavingsCash(taxiResponseDTO.getSavingsCash() != null ? (taxiResponseDTO.getSavingsCash() / 100.0) : null);
        dto.setSavingsProcent(taxiResponseDTO.getSavingsProcents());
        dto.setSharedRideOwner(Boolean.TRUE.equals(taxiResponseDTO.getSharedRideOwner()) ? "Да" : "Нет");
        dto.setRating(Optional.ofNullable(taxiResponseDTO.getRequestRating())
                              .map(RequestRatingDTO::getRating)
                              .map(String::valueOf)
                              .orElse(WITHOUT_RATING));
        dto.setRatingComment(getRatingComment(taxiResponseDTO));
        dto.setSharedRideId(Optional.ofNullable(taxiResponseDTO.getSharedRideId()).map(UUID::toString).orElse(null));
        dto.setRequestClosedDatetime(convertDateTime(taxiResponseDTO.getRequestClosedDatetime(), taxiResponseDTO.getTimeZone()));
        dto.setJoinedPassengers(taxiResponseDTO.getJoinedPassengers());
        dto.setSource(taxiResponseDTO.getSource());
        dto.setMinTaxiTariffCost(taxiResponseDTO.getMinTaxiTariffCost());
        dto.setTotalSharedRequestCount(taxiResponseDTO.getTotalSharedRequestCount());
        dto.setLimitDebit(taxiResponseDTO.getLimitDebit());
        dto.setDepartmentEconomy(taxiResponseDTO.getDepartmentEconomy());
        dto.setFinancialImpact(taxiResponseDTO.getFinancialImpact());
        dto.setFinancialImpactShared(taxiResponseDTO.getFinancialImpactShared());
        dto.setFinancialImpactJoined(taxiResponseDTO.getFinancialImpactJoined());
        dto.setCommentForPurpose(taxiResponseDTO.getCommentForPurpose());
        dto.setRequestStatusCode(taxiResponseDTO.getRequestStatusCode());
        dto.setDeadlineState(taxiResponseDTO.getDeadlineViolation().equalsIgnoreCase("да") ? "ДА" : "НЕТ");
        dto.setSavings(Boolean.TRUE.equals(taxiResponseDTO.getSavings()) ? "Есть" : "Нет");
        dto.setContractNumber(taxiResponseDTO.getContractNumber());
        dto.setTripClass(taxiResponseDTO.getTripClass());
        dto.setExecutorGroupId(String.valueOf(taxiResponseDTO.getExecutorGroupId()));
        dto.setExecutorGroupName(taxiResponseDTO.getExecutorGroupName());
        return dto;
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

    private void fillTaxiTripMaps(Page<TaxiResponseDTO> requests,
                                  Map<UUID, CoopTaxiTrip> coopTaxiTripMap,
                                  Map<UUID, SingleTaxiTrip> singleTaxiTripMap,
                                  Set<String> taxiTripIds) {

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
            return zdt.format(DateTimeFormatter.ofPattern(TaxiReportResolver.DATE_TIME_PATTERN));
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
    
    private String getPassengerPosition(TaxiResponseDTO r) {
        if (r.getPassenger() != null) {
            return r.getPassenger().getPositionName();
        } else {
            return null;
        }
    }
    
    private String getPurpose(TaxiResponseDTO r) {
        if (r.getPurpose() != null && r.getPurpose().getPurpose() != null) {
            return r.getPurpose().getPurpose();
        } else {
            return null;
        }
    }
    
    private Double getCost(TaxiResponseDTO r) {
        if (r.getExpected() != null) {
            return r.getExpected().getCost() / 100;
        } else {
            return null;
        }
    }
    
    private String getPassengerPersonnelNumber(TaxiResponseDTO r) {
        if (r.getPassenger() != null && r.getPassenger().getPersonnelNumber() != null) {
            return r.getPassenger().getPersonnelNumber();
        }
        return null;
    }
    
    private String getRatingComment(TaxiResponseDTO r) {
        if (r.getRequestRating() != null && r.getRequestRating().getRatingComment() != null) {
            return r.getRequestRating().getRatingComment();
        } else {
            return null;
        }
    }
    
    private Double getRequestExpectedDistance(TaxiResponseDTO r) {
        if (r.getExpected() != null) {
            return r.getExpected().getDistance();
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
    
    private String getContractorName(TaxiResponseDTO r) {
        var lim = r.getContractor();
        if (lim != null) {
            return lim.getName();
        } else {
            return null;
        }
    }
    
    private Double getTripFactDistance(TaxiTrip tt) {
        if (tt != null && tt.getTripFactDistance() != null) {
            return tt.getTripFactDistance();
        } else {
            return null;
        }
    }
    
    private Double getTripFactPrice(TaxiTrip tt) {
        if (tt != null && tt.getTripFactPrice() != null) {
            return tt.getTripFactPrice() / 100.0;
        } else {
            return null;
        }
    }
    
    private String getRequestExpectedTime(TaxiResponseDTO r) {
        if (r.getExpected() != null) {
            return String.valueOf(r.getExpected().getTime());
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
            return String.valueOf(tr.getTripFactWaitTime().toMinutes());
        }
        return null;
    }
}
