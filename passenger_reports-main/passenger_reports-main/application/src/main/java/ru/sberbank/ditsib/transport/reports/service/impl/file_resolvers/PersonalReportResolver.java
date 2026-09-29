package ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.EmployeeDTO;
import ru.sberbank.ditsib.transport.reports.dto.ExpectedDataDTO;
import ru.sberbank.ditsib.transport.reports.dto.PaymentDataDTO;
import ru.sberbank.ditsib.transport.reports.dto.RequestRatingDTO;
import ru.sberbank.ditsib.transport.reports.dto.WaypointDTO;
import ru.sberbank.ditsib.transport.reports.dto.files.PersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.personalTransport.PersonalCarDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PersonalResponseDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;
import ru.sberbank.ditsib.transport.reports.mappers.impl.StatusCodeHelper;
import ru.sberbank.ditsib.transport.reports.service.RequestService;

import java.time.*;
import java.util.*;

import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.CONFIDENTIAL;
import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.WITHOUT_RATING;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PersonalReportResolver implements DataExporter<PersonalReportDTO> {
    
    private static final String NOT_COOP = "Индивидуальная";
    private static final String COOP = "Совместная";
    private static final String DRIVER = "Водитель";
    private static final String PASSENGER = "Пассажир";
    private final RequestForXlsxMapper requestForXlsxMapper;
    private final RequestService requestService;
    
    @Override
    public String getCaption() {
        return CONFIDENTIAL;
    }
    
    @Override
    public List<PersonalReportDTO> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        log.info("PersonalReportResolver: exportData start1");
        RequestForPersonalReportDTO filters;
        filters = getFilters(parameters);
        return requestService.findPersonalRequests(filters)
                .map(this::mapToPersonalReportDTO)
                .toList();
    }

    private PersonalReportDTO mapToPersonalReportDTO(PersonalResponseDTO request) {
        var dto = new PersonalReportDTO();
        dto.setMvz(request.getCostCenter());
        dto.setHumanReadableId(request.getHumanReadableId());
        dto.setCreationTime(convertDateTime(request.getCreationTime(), request.getTimeZone()));
        dto.setActualDepartureDatetime(convertDateTime(request.getTripStartTime(), request.getTimeZone()));
        dto.setApprovalDate(convertDateTime(request.getApproveDate(), request.getTimeZone()));
        dto.setPaymentDate(convertDateTime(request.getPaymentTime(),
                        null
                ));
        dto.setFio(getPassengerFio(request));
        dto.setPassengerActivity(getPassengerActivity(request));
        dto.setPurpose(getPurpose(request));
        dto.setStatus(Optional.ofNullable(request.getStatus()).map(TripRequestStatus::getDescription).orElse(null));
        dto.setStatusCode(StatusCodeHelper.getStatusCodeNameForPersonal(request.getStatusCode()));
        dto.setRequestExpectedCost(getCost(request));
        dto.setWaypointsCount(Optional.ofNullable(request.getExpected()).map(ExpectedDataDTO::getWaypointsCount).orElse(0));
        dto.setWaypointsCountWithCheckIn(
                Long.valueOf(Optional.ofNullable(request.getExpected()).map(ExpectedDataDTO::getWaypointsCountWithCheckIn).orElse(0)));
        dto.setWaypointsCountWithoutCheckIn(
                Long.valueOf(Optional.ofNullable(request.getExpected()).map(ExpectedDataDTO::getWaypointsCountWithoutCheckIn).orElse(0)));
        dto.setPaymentPeriod(request.getPaymentPeriod());
        dto.setPaymentCost(request.getPaymentCost() / 100.0);
        dto.setDepartmentCode(Optional.ofNullable(request.getDepartment()).map(DepartmentShortDTO::getCode).orElse(null));
        dto.setPassengerDepartment1(request.getPassengerDepartment1());
        dto.setPassengerDepartment2(request.getPassengerDepartment2());
        dto.setPassengerDepartment3(request.getPassengerDepartment3());
        dto.setPassengerDepartment4(request.getPassengerDepartment4());
        dto.setPassengerDepartment5(request.getPassengerDepartment5());
        dto.setPassengerDepartment6(request.getPassengerDepartment6());
        dto.setTariff(request.getTariff());
        dto.setDepartureAddress(request.getDepartureAddress());
        dto.setDestinationAddress(request.getDestinationAddress());
        dto.setIntermediateAddresses(request.getIntermediateAddresses());
        dto.setPassengerPersonnelNumber(
                Optional.ofNullable(request.getPassenger()).map(EmployeeDTO::getPersonnelNumber).orElse(null));
        dto.setOrderPaymentFormationStartDate(convertDateTime(request.getOrderPaymentFormationStartDate(), request.getTimeZone()));
        dto.setRating(Optional.ofNullable(request.getRequestRating())
                              .map(RequestRatingDTO::getRating)
                              .map(String::valueOf)
                              .orElse(WITHOUT_RATING));
        dto.setRatingComment(Optional.ofNullable(request.getRequestRating()).map(RequestRatingDTO::getRatingComment).orElse(null));
        dto.setOwnershipOfCar(Optional.ofNullable(request.getPersonalCar()).map(PersonalCarDTO::getOwnerInfo).orElse(null));
        dto.setMarriageCertificateNumber(
                Optional.ofNullable(request.getPassenger()).map(EmployeeDTO::getMarriageCertificateNumber).orElse(null));
        dto.setCarRegistrationNumber(
                Optional.ofNullable(request.getPersonalCar()).map(PersonalCarDTO::getRegistrationNumber).orElse(null));
        dto.setCarBrandName(Optional.ofNullable(request.getPersonalCar()).map(PersonalCarDTO::getBrandName).orElse(null));
        dto.setCarEngineVolume(Optional.ofNullable(request.getPersonalCar()).map(PersonalCarDTO::getEngineVolume).orElse(null));
        dto.setOsagoNumber(Optional.ofNullable(request.getPersonalCar()).map(PersonalCarDTO::getInsuranceNumber).orElse(null));
        dto.setRequestExpectedDistance(getRequestExpectedDistance(request));
        dto.setDesiredDate(convertDate(request.getDesiredDate(), request.getTimeZone()));
        dto.setDesiredTime(convertTime(request.getDesiredDate(), request.getTimeZone()));
        dto.setCostSharedPart(request.getCostSharePart());
        dto.setSavingsCash(request.getSavingsCash() != null ? (request.getSavingsCash() / 100.0) : null);
        dto.setSavingsProcent(request.getSavingsProcents());
        dto.setDriverFIO(request.getDriverFIO());
        dto.setSharedRideOwner(request.getSharedRideOwner());
        dto.setDriverOrPassenger(request.isCoopTrip() && !Boolean.TRUE.equals(request.getSharedRideOwner()) ? PASSENGER : DRIVER);
        dto.setNumberPassengersJoined(request.getNumberPassengersJoined());
        dto.setAdditionalSum(request.getAdditionalSum() != null ? (request.getAdditionalSum() / 100.0) : null);
        dto.setTripType(request.isCoopTrip() ? COOP : NOT_COOP);
        dto.setPassengerCount(request.getPassengerCount());
        dto.setSum4661(getSumByCode(request, PaymentTypeCode.CODE_4661));
        dto.setSum4664(getSumByCode(request, PaymentTypeCode.CODE_4664));
        dto.setSum4665(getSumByCode(request, PaymentTypeCode.CODE_4665));
        dto.setJoinedPassengers(request.getJoinedPassengers());
        dto.setSource(request.getSource());
        dto.setMinTaxiTariffCost(request.getMinTaxiTariffCost());
        dto.setTotalSharedRequestCount(request.getTotalSharedRequestCount());
        dto.setLimitDebit(request.getLimitDebit());
        dto.setDepartmentEconomy(request.getDepartmentEconomy());
        dto.setFinancialImpact(request.getFinancialImpact());
        dto.setFinancialImpactShared(request.getFinancialImpactShared());
        dto.setFinancialImpactJoined(request.getFinancialImpactJoined());
        dto.setCommentForPurpose(request.getCommentForPurpose());
        dto.setRequestStatusCode(request.getRequestStatusCode());
        dto.setSavings(Boolean.TRUE.equals(request.getSavings()) ? "Есть" : "Нет");
        dto.setRequestClosedDatetime(convertDateTime(request.getRequestClosedDatetime(), request.getTimeZone()));
        dto.setDeadline(convertDateTime(request.getDeadline(), request.getTimeZone()));
        dto.setDeadlineState(request.getDeadlineViolation().equalsIgnoreCase("да") ? "ДА" : "НЕТ");
        dto.setWaypointsCountWithAutoCheckIn(getWaypointsCountWithAutoCheckIn(request.getExpected()));
        dto.setWaypointsCountWithManualCheckIn(getWaypointsCountWithManualCheckIn(request.getExpected()));
        dto.setExecutorGroupId(String.valueOf(request.getExecutorGroupId()));
        dto.setExecutorGroupName(request.getExecutorGroupName());
        return dto;
    }

    private RequestForPersonalReportDTO getFilters(Map<String, ?> parameters) {
        var filters = requestForXlsxMapper.toPersonalDto(parameters);
        if (filters.getOrderPaymentFormationStartRange() == null
                && filters.getDesiredDateRange() == null
                && filters.getCreationDate() == null
                && filters.getApproveDate() == null
                && filters.getOrderPaymentFormationFinishingDate() == null
                && filters.getRequestHumanId() == null) {
            filters.setCreationDate(requestForXlsxMapper.getCurrentYear());
        }
        var pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(Integer.MAX_VALUE);
        filters.setPageSetting(pageSetting);

        return filters;
    }
    
    private Long getWaypointsCountWithManualCheckIn(ExpectedDataDTO expected) {
        return Optional.ofNullable(expected)
                       .map(ExpectedDataDTO::getWaypoints)
                       .orElseGet(Collections::emptyList)
                       .stream()
                       .filter(WaypointDTO::getCheckinManual)
                       .count();
    }
    
    private Long getWaypointsCountWithAutoCheckIn(ExpectedDataDTO expected) {
        return Optional.ofNullable(expected)
                       .map(ExpectedDataDTO::getWaypoints)
                       .orElseGet(Collections::emptyList)
                       .stream()
                       .filter(WaypointDTO::getCheckinAutomatic)
                       .count();
    }
    
    private Double getSumByCode(PersonalResponseDTO request, PaymentTypeCode paymentTypeCode) {
        return request.getPaymentDataList().stream()
                .filter(paymentDataDTO -> paymentTypeCode.equals(paymentDataDTO.getPaymentTypeCode()))
                .mapToLong(PaymentDataDTO::getPaymentPrice)
                .sum() / 100.0;
    }
    
    private LocalDateTime convertDateTime(LocalDateTime dateTime, String timeZone) {
        if (dateTime != null) {
            final var zdt = dateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(timeZone == null ? "UTC" : timeZone));
            return zdt.toLocalDateTime();
        } else {
            return null;
        }
    }
    
    private LocalDate convertDate(LocalDateTime dateTime, String timeZone) {
        if (dateTime != null) {
            final var zdt = dateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(timeZone == null ? "UTC" : timeZone));
            return zdt.toLocalDate();
        } else {
            return null;
        }
    }
    
    private LocalTime convertTime(LocalDateTime dateTime, String timeZone) {
        if (dateTime != null) {
            final var zdt = dateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(timeZone == null ? "UTC" : timeZone));
            return zdt.toLocalTime();
        } else {
            return null;
        }
    }
    
    private String getPassengerFio(PersonalResponseDTO r) {
        if (r.getPassenger() != null) {
            return r.getPassenger().getLastName() + " " +
                   r.getPassenger().getFirstName() +
                   (r.getPassenger().getPatronymic() == null ? "" : (" " + r.getPassenger().getPatronymic()));
        } else {
            return null;
        }
    }
    
    private String getPassengerActivity(PersonalResponseDTO r) {
        if (r.getPassenger() != null && r.getPassenger().getItinerantType() != null) {
            return "Да";
        } else {
            return "Нет";
        }
    }
    
    private String getPurpose(PersonalResponseDTO r) {
        if (r.getPurpose() != null && r.getPurpose().getPurpose() != null) {
            return r.getPurpose().getPurpose();
        } else {
            return null;
        }
    }
    
    private Double getCost(PersonalResponseDTO r) {
        if (r.getExpected() != null) {
            return r.getExpected().getCost() / 100;
        } else {
            return null;
        }
    }
    
    private Double getRequestExpectedDistance(PersonalResponseDTO r) {
        if (r.getExpected() != null) {
            return r.getExpected().getDistance();
        } else {
            return null;
        }
    }
}
