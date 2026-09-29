package ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.ExpectedDataDTO;
import ru.sberbank.ditsib.transport.reports.dto.RequestRatingDTO;
import ru.sberbank.ditsib.transport.reports.dto.TariffShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.files.GroupTransferReportRegisterDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.GroupTransferResponseDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;
import ru.sberbank.ditsib.transport.reports.mappers.impl.StatusCodeHelper;
import ru.sberbank.ditsib.transport.reports.service.RequestRegisterService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.CONFIDENTIAL;
import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.WITHOUT_RATING;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GroupTransferReportRegisterResolver implements DataExporter<GroupTransferReportRegisterDTO> {
    
    private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm:ss";
    private static final String DATE_PATTERN = "dd.MM.yyyy";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DURATION_FORMAT = "%02d:%02d:%02d";
    private final RequestForXlsxMapper requestForXlsxMapper;
    private final RequestRegisterService requestRegisterService;
    
    @Override
    public String getCaption() {
        return CONFIDENTIAL;
    }
    
    @Override
    public List<GroupTransferReportRegisterDTO> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        log.debug("GroupTransferReportResolver: exportData start1");
        var result = new ArrayList<GroupTransferReportRegisterDTO>();
        var filters = requestForXlsxMapper.toGroupTransferDto(parameters);
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
        
        var groupTransferRequests = requestRegisterService.findGroupTransferRequests(filters);
        
        groupTransferRequests.forEach(responseDTO -> {
            
            var dto = new GroupTransferReportRegisterDTO();
            dto.setOrganizationOfficialName(responseDTO.getOrganizationOfficialName());
            dto.setRequestId(responseDTO.getHumanReadableId());
            dto.setMvz(responseDTO.getCostCenter());
            dto.setCreationDateTime(
                    convertDateTime(responseDTO.getCreationTime(), responseDTO.getTimeZone()));
            dto.setRequestStatus(
                    Optional.ofNullable(responseDTO.getStatus()).map(TripRequestStatus::getDescription).orElse(null));
            dto.setStatusCode(StatusCodeHelper.getStatusCodeNameForGroupTransfer(responseDTO.getRequestStatusCode()));
            dto.setGroupTransferClass(
                    Optional.ofNullable(responseDTO.getGroupTransferClass()).map(GroupTransferClass::getRusName).orElse(""));
            dto.setDepartureAddress(responseDTO.getDepartureAddress());
            dto.setIntermediateAddresses(responseDTO.getIntermediateAddresses());
            dto.setWaypointsCount(
                    Optional.ofNullable(responseDTO.getExpected()).map(ExpectedDataDTO::getWaypointsCount).orElse(0));
            dto.setWaypointWaitTime(getWaypointWaitTime(responseDTO));
            dto.setDestinationAddress(responseDTO.getDestinationAddress());
            
            // В качестве даты/времени выполнения заявки считаем дату/время прибытия водителя в точку отправления
            dto.setExecutionDateOfRequest(
                    convertDateTime(responseDTO.getDriverArrivedDatetime(), responseDTO.getTimeZone()));
            
            dto.setFinishedTime(
                    convertDateTime(responseDTO.getFinishedTime(), responseDTO.getTimeZone()));
            dto.setDesiredTime(getDesiredTime(responseDTO));
            dto.setDesiredDate(getDesiredDate(responseDTO));
            dto.setDeadline(convertDateTime(responseDTO.getDeadline(), responseDTO.getTimeZone()));
            dto.setDriverArrivedDatetime(
                    convertDateTime(responseDTO.getDriverArrivedDatetime(), responseDTO.getTimeZone()));
            dto.setDeadlineViolation(responseDTO.getDeadlineViolation());
            dto.setCustomerPersonnelNumber(getCustomerPersonnelNumber(responseDTO));
            dto.setCustomerFio(getCustomerFio(responseDTO));
            dto.setPassengerActivity(getPassengerActivity(responseDTO));
            dto.setPassengerCount(responseDTO.getPassengerCount());
            dto.setTripPurpose(getPurpose(responseDTO));
            dto.setLimitId(getLimitId(responseDTO));
            dto.setCommentForDriver(responseDTO.getCommentForDriver());
            dto.setDepartmentCode(
                    Optional.ofNullable(responseDTO.getDepartment()).map(DepartmentShortDTO::getCode).orElse(null));
            dto.setPassengerDepartment1(responseDTO.getPassengerDepartment1());
            dto.setPassengerDepartment2(responseDTO.getPassengerDepartment2());
            dto.setPassengerDepartment3(responseDTO.getPassengerDepartment3());
            dto.setPassengerDepartment4(responseDTO.getPassengerDepartment4());
            dto.setPassengerDepartment5(responseDTO.getPassengerDepartment5());
            dto.setPassengerDepartment6(responseDTO.getPassengerDepartment6());
            dto.setPassengerPosition(getPassengerPosition(responseDTO));
            dto.setPassengerPersonnelNumber(getPassengerPersonnelNumber(responseDTO));
            dto.setPassengerFio(getPassengerFio(responseDTO));
            dto.setApproverPersonnelNumber(getApproverPersonnelNumber(responseDTO));
            dto.setApproverFio(getApproverFio(responseDTO));
            dto.setTariffId(
                    Optional.ofNullable(responseDTO.getTariff()).map(TariffShortDTO::getHumanReadableId).orElse(null));
            dto.setPassengerMobilePhone(getPassengerMobilePhone(responseDTO));
            dto.setWorkGroup(
                    Optional.ofNullable(responseDTO.getTariff()).map(TariffShortDTO::getWorkGroup).orElse(null));
            dto.setContractorName(getContractorName(responseDTO));
            dto.setRequestExpectedDistance(getRequestExpectedDistance(responseDTO));
            dto.setRequestExpectedCost(getCost(responseDTO));
            dto.setRequestExpectedTime(getRequestExpectedTime(responseDTO));
            dto.setRating(Optional.ofNullable(responseDTO.getRequestRating())
                                  .map(RequestRatingDTO::getRating)
                                  .map(String::valueOf)
                                  .orElse(WITHOUT_RATING));
            dto.setRatingComment(getRatingComment(responseDTO));
            dto.setRequestClosedDatetime(
                    convertDateTime(responseDTO.getRequestClosedDatetime(), responseDTO.getTimeZone()));
            dto.setVip(responseDTO.getVip());
            dto.setSource(responseDTO.getSource());
            dto.setCommentForPurpose(responseDTO.getCommentForPurpose());
            dto.setRequestStatusCode(responseDTO.getRequestStatusCode());
            dto.setDeadlineState(responseDTO.getDeadlineViolation().equalsIgnoreCase("да") ? "ДА" : "НЕТ");
            dto.setSavings(Boolean.TRUE.equals(responseDTO.getSavings()) ? "Есть" : "Нет");
            dto.setContractNumber(responseDTO.getContractNumber());
            dto.setMinTaxiTariffCost(responseDTO.getMinTaxiTariffCost());
            dto.setExecutorGroupId(String.valueOf(responseDTO.getExecutorGroupId()));
            dto.setExecutorGroupName(responseDTO.getExecutorGroupName());
            result.add(dto);
        });
        return result;
    }
    
    
    private String convertDateTime(LocalDateTime dateTime, String timeZone) {
        if (dateTime != null) {
            ZonedDateTime zdt = dateTime.atZone(ZoneOffset.UTC);
            zdt = zdt.withZoneSameInstant(ZoneId.of(timeZone == null ? "UTC" : timeZone));
            return zdt.format(DateTimeFormatter.ofPattern(GroupTransferReportRegisterResolver.DATE_TIME_PATTERN));
        } else {
            return null;
        }
    }

    private String getPassengerFio(GroupTransferResponseDTO r) {
        if (r.getPassenger() != null) {
            return r.getPassenger().getLastName() + " " +
                   r.getPassenger().getFirstName() +
                   (r.getPassenger().getPatronymic() == null ? "" : (" " + r.getPassenger().getPatronymic()));
        } else {
            return null;
        }
    }
    
    private String getPassengerActivity(GroupTransferResponseDTO r) {
        if (r.getPassenger() != null && r.getPassenger().getItinerantType() != null) {
            return "Да";
        } else {
            return "Нет";
        }
    }
    
    private String getPassengerPosition(GroupTransferResponseDTO r) {
        if (r.getPassenger() != null) {
            return r.getPassenger().getPositionName();
        } else {
            return null;
        }
    }
    
    private String getPurpose(GroupTransferResponseDTO r) {
        if (r.getPurpose() != null && r.getPurpose().getPurpose() != null) {
            return r.getPurpose().getPurpose();
        } else {
            return null;
        }
    }
    
    private Double getCost(GroupTransferResponseDTO r) {
        if (r.getExpected() != null) {
            return r.getExpected().getCost() / 100;
        } else {
            return null;
        }
    }
    
    private String getPassengerPersonnelNumber(GroupTransferResponseDTO r) {
        if (r.getPassenger() != null && r.getPassenger().getPersonnelNumber() != null) {
            return r.getPassenger().getPersonnelNumber();
        }
        return null;
    }
    
    private String getRatingComment(GroupTransferResponseDTO r) {
        if (r.getRequestRating() != null && r.getRequestRating().getRatingComment() != null) {
            return r.getRequestRating().getRatingComment();
        } else {
            return null;
        }
    }
    
    private Double getRequestExpectedDistance(GroupTransferResponseDTO r) {
        if (r.getExpected() != null) {
            return r.getExpected().getDistance();
        } else {
            return null;
        }
    }
    
    private String getDesiredDate(GroupTransferResponseDTO r) {
        if (r.getDesiredDate() != null) {
            ZonedDateTime zdt = r.getDesiredDate().atZone(ZoneId.of("UTC"));
            zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
            return zdt.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        } else {
            return null;
        }
    }
    
    private String getDesiredTime(GroupTransferResponseDTO r) {
        if (r.getDesiredDate() != null) {
            ZonedDateTime zdt = r.getDesiredDate().atZone(ZoneId.of("UTC"));
            zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
            return zdt.format(DateTimeFormatter.ofPattern(TIME_PATTERN));
        } else {
            return null;
        }
    }
    
    private String getLimitId(GroupTransferResponseDTO r) {
        var lim = r.getLimit();
        if (lim != null) {
            return lim.getHumanReadableId();
        } else {
            return null;
        }
    }
    
    private String getCustomerPersonnelNumber(GroupTransferResponseDTO r) {
        if (r.getAuthor() != null && r.getAuthor().getPersonnelNumber() != null) {
            return r.getAuthor().getPersonnelNumber();
        } else {
            return null;
        }
    }
    
    private String getCustomerFio(GroupTransferResponseDTO r) {
        if (r.getAuthor() != null) {
            return r.getAuthor().getLastName() + " " + r.getAuthor().getFirstName() + " " + r.getAuthor().getPatronymic();
        } else {
            return null;
        }
    }
    
    private String getApproverPersonnelNumber(GroupTransferResponseDTO r) {
        if (r.getApprovedBy() != null && r.getApprovedBy().getPersonnelNumber() != null) {
            return r.getApprovedBy().getPersonnelNumber();
        } else {
            return null;
        }
    }
    
    private String getApproverFio(GroupTransferResponseDTO r) {
        if (r.getApprovedBy() != null) {
            return r.getApprovedBy().getLastName() + " " + r.getApprovedBy().getFirstName() + " " + r.getApprovedBy().getPatronymic();
        } else {
            return null;
        }
    }
    
    private String getPassengerMobilePhone(GroupTransferResponseDTO r) {
        if (r.getPassenger() != null
            && r.getPassenger().getPhone() != null
            && !r.getPassenger().getPhone().isBlank()) {
            return r.getPassenger().getPhone();
        } else {
            return null;
        }
    }
    
    private String getContractorName(GroupTransferResponseDTO r) {
        var lim = r.getContractor();
        if (lim != null) {
            return lim.getName();
        } else {
            return null;
        }
    }
    
    private String getRequestExpectedTime(GroupTransferResponseDTO r) {
        if (r.getExpected() == null || r.getExpected().getExpectedTime() == null) {
            return null;
        }
        var expectedTime = r.getExpected().getExpectedTime();
        return expectedTime.toHours() != 0 ?
                String.format("%dч %dмин", expectedTime.toHours(), expectedTime.toMinutesPart()) :
                String.format("%dмин", expectedTime.toMinutesPart());
    }
    
    private String getWaypointWaitTime(GroupTransferResponseDTO r) {
        Duration resultWaitTime = Duration.ZERO;
        if (r.getExpected().getWaypoints().size() > 2) {
            for (int i = 1; i < r.getExpected().getWaypoints().size() - 1; i++) {
                resultWaitTime = resultWaitTime.plus(r.getExpected().getWaypoints().get(i).getWaitTime(), ChronoUnit.MINUTES);
            }
        }
        return String.format(DURATION_FORMAT, resultWaitTime.toHours(), resultWaitTime.toMinutesPart(), resultWaitTime.toSecondsPart());
    }
}
