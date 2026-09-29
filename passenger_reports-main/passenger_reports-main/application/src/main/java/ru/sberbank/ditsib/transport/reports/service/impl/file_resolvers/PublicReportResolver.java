package ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.ExpectedDataDTO;
import ru.sberbank.ditsib.transport.reports.dto.RequestRatingDTO;
import ru.sberbank.ditsib.transport.reports.dto.TariffShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.WaypointDTO;
import ru.sberbank.ditsib.transport.reports.dto.files.PublicReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.publicTransport.TransportCompensationDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PublicResponseDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;
import ru.sberbank.ditsib.transport.reports.service.RequestService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.CONFIDENTIAL;
import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.WITHOUT_RATING;

@Component
@RequiredArgsConstructor
@Transactional
public class PublicReportResolver implements DataExporter<PublicReportDTO> {
    
    private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm:ss";
    private final RequestForXlsxMapper requestForXlsxMapper;
    private final RequestService requestService;
    
    @Override
    public String getCaption() {
        return CONFIDENTIAL;
    }
    
    @Override
    public List<PublicReportDTO> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        var result = new ArrayList<PublicReportDTO>();
        var filters = getFilters(parameters);
        
        var requests = requestService.findPublicRequests(filters);
        
        requests.forEach(request -> {
            var dto = new PublicReportDTO();
            dto.setMvz(request.getCostCenter());
            dto.setHumanReadableId(request.getHumanReadableId());
            dto.setCreationTime(getCreationTime(request));
            dto.setApprovalDate(getApprovalDate(request));
            dto.setPaymentDate(getPaymentDate(request));
            dto.setFio(getPassengerFio(request));
            dto.setPassengerActivity(getPassengerActivity(request));
            dto.setPurpose(getPurpose(request));
            dto.setTransportType(request.getTransportType());
            dto.setStatus(Optional.ofNullable(request.getStatus()).map(TripRequestStatus::getDescription).orElse(null));
            dto.setRequestExpectedCost(getCost(request));
            dto.setRequestExpectedDistance(request.getExpected() != null ? request.getExpected().getDistance() : null);
            dto.setHasAttachment(Boolean.TRUE.equals(request.getPublicCompensationDocumentExist()) ? "ДА" : "НЕТ");
            dto.setPaymentPeriod(getPaymentPeriod(request));
            dto.setDepartmentCode(getDepartmentCode(request));
            dto.setPassengerDepartment1(getPassengerDepartmentLevel(request, 1));
            dto.setPassengerDepartment2(getPassengerDepartmentLevel(request, 2));
            dto.setPassengerDepartment3(getPassengerDepartmentLevel(request, 3));
            dto.setPassengerDepartment4(getPassengerDepartmentLevel(request, 4));
            dto.setPassengerDepartment5(getPassengerDepartmentLevel(request, 5));
            dto.setPassengerDepartment6(getPassengerDepartmentLevel(request, 6));
            dto.setCompensationType(getCompensationType(request));
            dto.setDepartureAddress(getDepartureAddress(request));
            dto.setDestinationAddress(getDestinationAddress(request));
            dto.setIntermediateAddresses(getIntermediateAddresses(request));
            dto.setPassengerPersonnelNumber(getPassengerPersonnelNumber(request));
            dto.setOrderPaymentFormationStartDate(getOrderPaymentFormationStartDate(request));
            dto.setRating(Optional.ofNullable(request.getRequestRating())
                                  .map(RequestRatingDTO::getRating)
                                  .map(String::valueOf)
                                  .orElse(WITHOUT_RATING));
            dto.setRatingComment(getRatingComment(request));
            dto.setSum4664(getSumByCode(request, PaymentTypeCode.CODE_4664) / 100);
            dto.setSum4666(getSumByCode(request, PaymentTypeCode.CODE_4666) / 100);
            dto.setPaymentCost(calcPaymentCost(dto.getSum4664(), dto.getSum4666()));
            dto.setSource(request.getSource());
            dto.setMinTaxiTariffCost(request.getMinTaxiTariffCost());
            dto.setCommentForPurpose(request.getCommentForPurpose());
            dto.setRequestStatusCode(request.getRequestStatusCode());
            dto.setSavings(Boolean.TRUE.equals(request.getSavings()) ? "Есть" : "Нет");
            dto.setRequestClosedDatetime(convertDateTime(request.getRequestClosedDatetime(), request.getTimeZone()));
            dto.setDeadline(convertDateTime(request.getDeadline(), request.getTimeZone()));
            dto.setDeadlineState(request.getDeadlineViolation().equalsIgnoreCase("да") ? "ДА" : "НЕТ");
            dto.setWaypointsCountWithAutoCheckIn(getWaypointsCountWithAutoCheckIn(request.getExpected()));
            dto.setWaypointsCountWithManualCheckIn(getWaypointsCountWithManualCheckIn(request.getExpected()));
            dto.setWaypointsCountWithoutCheckIn(Optional.of(getWaypointsCountWithoutCheckIn(request.getExpected())).orElse(0L));
            dto.setWaypointsCount(Optional.of(getWaypointsCount(request.getExpected())).orElse(0));
            dto.setExecutorGroupId(String.valueOf(request.getExecutorGroupId()));
            dto.setExecutorGroupName(request.getExecutorGroupName());
            dto.setTariffName(Optional.ofNullable(request.getTariff()).map(TariffShortDTO::getHumanReadableId).orElse(null));
            dto.setPublicTransportType(request.getTransportCompensation().get(0).getTransportType().getRusName());
            dto.setPublicCompensationDocumentExist(request.getPublicCompensationDocumentExist());
            dto.setAttachedDocuments((int) request.getTransportCompensation().stream().map(TransportCompensationDTO::getAttachedDocumentId).filter(Objects::nonNull).count());
            result.add(dto);
        });
        return result;
    }
    
    private RequestForPublicReportDTO getFilters(Map<String, ?> parameters) {
        var filters = requestForXlsxMapper.toPublicDto(parameters);
        
        var pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(Integer.MAX_VALUE);
        filters.setPageSetting(pageSetting);
        
        return filters;
    }
    
    private LocalDateTime convertDateTime(LocalDateTime dateTime, String timeZone) {
        if (dateTime != null) {
            final var zdt = dateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(timeZone == null ? "UTC" : timeZone));
            return zdt.toLocalDateTime();
        } else {
            return null;
        }
    }
    
    private Long getWaypointsCountWithoutCheckIn(ExpectedDataDTO expected) {
        return getWaypointsCount(expected) - (getWaypointsCountWithAutoCheckIn(expected) + getWaypointsCountWithManualCheckIn(expected));
    }
    
    private Integer getWaypointsCount(ExpectedDataDTO expected) {
        return expected.getWaypoints().size();
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
    
    private Double calcPaymentCost(Double sum4664, Double sum4666) {
        return Optional.ofNullable(sum4664).orElse(0.0) * 0.87 + Optional.ofNullable(sum4666).orElse(0.0);
    }
    
    private Double getSumByCode(PublicResponseDTO request, PaymentTypeCode paymentTypeCode) {
        Double result = 0.0d;
        if (request != null && paymentTypeCode != null) {
            if (request.getPaymentTypeCodeMain() != null && paymentTypeCode.getCode() == request.getPaymentTypeCodeMain()) {
                result += request.getPaymentPriceMain();
            }
            if (request.getPaymentTypeCodeOptional() != null && paymentTypeCode.getCode() == request.getPaymentTypeCodeOptional()) {
                result += request.getPaymentPriceOptional();
            }
        }
        return result;
    }
    
    private LocalDateTime getCreationTime(PublicResponseDTO r) {
        final var creationTimeLocalDateTime = r.getCreationTime();
        final var zdt = creationTimeLocalDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
        return zdt.toLocalDateTime();
    }
    
    private LocalDateTime getApprovalDate(PublicResponseDTO r) {
        if (r.getApproveDate() != null) {
            final var approvalDateLocalDateTime = r.getApproveDate().atZone(ZoneId.of("UTC"));
            final var zdt = approvalDateLocalDateTime.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
            
            return zdt.toLocalDateTime();
        } else {
            return null;
        }
    }
    
    private LocalDateTime getPaymentDate(PublicResponseDTO r) {
        return r.getPaymentTime();
    }
    
    private String getPassengerFio(PublicResponseDTO r) {
        if (r.getPassenger() != null) {
            return r.getPassenger().getFIO();
        } else {
            return null;
        }
    }
    
    private String getPassengerActivity(PublicResponseDTO r) {
        if (r.getPassenger() != null && r.getPassenger().getItinerantType() != null) {
            return "Да";
        } else {
            return "Нет";
        }
    }
    
    private String getPurpose(PublicResponseDTO r) {
        if (r.getPurpose() != null && r.getPurpose().getPurpose() != null) {
            return r.getPurpose().getPurpose();
        } else {
            return null;
        }
    }
    
    private Integer getPaymentPeriod(PublicResponseDTO r) {
        return r.getPaymentQuarter();
    }
    
    private String getDepartmentCode(PublicResponseDTO publicResponseDTO) {
        return publicResponseDTO.getDepartment().getCode();
    }
    
    private String getPassengerDepartmentLevel(PublicResponseDTO r, int index) {
        return switch (index) {
            case 1 -> r.getPassengerDepartment1();
            case 2 -> r.getPassengerDepartment2();
            case 3 -> r.getPassengerDepartment3();
            case 4 -> r.getPassengerDepartment4();
            case 5 -> r.getPassengerDepartment5();
            case 6 -> r.getPassengerDepartment6();
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }
    
    private Double getCost(PublicResponseDTO r) {
        if (r.getExpected() != null) {
            return r.getExpected().getCost() / 100;
        } else {
            return null;
        }
    }
    
    private String getCompensationType(PublicResponseDTO r) {
        if (r.getTransportCompensation() != null) {
            return r.getTransportCompensation().stream()
                    .map(v -> v.getCompensationType().getRusName()).collect(Collectors.joining(", "));
        }
        return null;
    }
    
    private String getDepartureAddress(PublicResponseDTO r) {
        return r.getDepartureAddress();
    }
    
    private String getDestinationAddress(PublicResponseDTO r) {
        return r.getDestinationAddress();
    }
    
    private String getIntermediateAddresses(PublicResponseDTO r) {
        return r.getIntermediateAddresses();
    }
    
    private String getPassengerPersonnelNumber(PublicResponseDTO r) {
        if (r.getPassenger() != null && r.getPassenger().getPersonnelNumber() != null) {
            return r.getPassenger().getPersonnelNumber();
        }
        return null;
    }
    
    private String getOrderPaymentFormationStartDate(PublicResponseDTO r) {
        if (r.getOrderPaymentFormationStartDate() != null) {
            LocalDateTime orderPaymentFormationStartDate = r.getOrderPaymentFormationStartDate();
            ZonedDateTime zdt = orderPaymentFormationStartDate.atZone(ZoneId.of("UTC"));
            zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
            return zdt.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        } else {
            return null;
        }
    }
    
    private String getRatingComment(PublicResponseDTO r) {
        var rating = r.getRequestRating();
        if (rating != null && rating.getRatingComment() != null) {
            return rating.getRatingComment();
        } else {
            return null;
        }
    }
    
}
