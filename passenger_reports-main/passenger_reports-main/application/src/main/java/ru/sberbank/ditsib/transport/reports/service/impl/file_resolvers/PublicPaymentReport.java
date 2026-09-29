package ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.files.PublicPaymentDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;
import ru.sberbank.ditsib.transport.reports.messaging.senders.UpdateRequestStatusFromReportsSender;
import ru.sberbank.ditsib.transport.reports.service.RequestService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.PUBLIC_PAYMENT_AWAITING;
import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.CONFIDENTIAL;

@Component
@RequiredArgsConstructor
@Transactional
public class PublicPaymentReport implements DataExporter<PublicPaymentDTO> {
    
    private static final String RESOURCE_VALUE = "26511";
    private final RequestForXlsxMapper requestForXlsxMapper;
    private final RequestService requestService;
    private final UpdateRequestStatusFromReportsSender statusSender;
    
    @Override
    public String getCaption() {
        return CONFIDENTIAL;
    }
    
    @Override
    public List<PublicPaymentDTO> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        var result = new ArrayList<PublicPaymentDTO>();
        var filters = getFiltersForExportData(parameters);
    
        var requests = requestService.findPublicRequestsForPaymentAndAggregation(filters);
        var forSaving = requests.parallelStream().filter(r -> PUBLIC_ORDER_PAYMENT_FORMATION.name().equals(r.getStatus())).toList();
        forSaving.parallelStream().forEach(request -> {
            request.setStatus(PUBLIC_PAYMENT_AWAITING.name());
            request.setOrderPaymentFormationFinishingDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        });
        var saved = requestService.save(forSaving);
        saved.forEach(r -> statusSender.send(r.getId(), r.getStatus(), r.getOrderPaymentFormationFinishingDate(), userId));
        requests.forEach(request -> {
            var publicPaymentMainDTO = new PublicPaymentDTO();
            publicPaymentMainDTO.setHumanReadableId(request.getHumanReadableId());
            publicPaymentMainDTO.setPaymentCode(request.getPaymentData().getPaymentTypeCodeMain().getCode());
            publicPaymentMainDTO.setResource(RESOURCE_VALUE);
            publicPaymentMainDTO.setMoney(getMoney(request.getPaymentData().getPaymentPriceMain()));
            publicPaymentMainDTO.setPersonnelNumber(request.getPassenger().getPersonnelNumber());
            publicPaymentMainDTO.setFio(request.getPassenger().getFIO());
            publicPaymentMainDTO.setPeriodFormationForPayment(getPeriodFormationForPayment(request.getOrderPaymentFormationStartDate()));
            result.add(publicPaymentMainDTO);
            if (request.getPaymentData().thereIsOptionalPayment()) {
                var publicPaymentOptionalDTO = new PublicPaymentDTO();
                publicPaymentOptionalDTO.setHumanReadableId(request.getHumanReadableId());
                publicPaymentOptionalDTO.setPaymentCode(request.getPaymentData().getPaymentTypeCodeOptional().getCode());
                publicPaymentOptionalDTO.setResource(RESOURCE_VALUE);
                publicPaymentOptionalDTO.setMoney(getMoney(request.getPaymentData().getPaymentPriceOptional()));
                publicPaymentOptionalDTO.setPersonnelNumber(request.getPassenger().getPersonnelNumber());
                publicPaymentOptionalDTO.setFio(request.getPassenger().getFIO());
                publicPaymentOptionalDTO.setPeriodFormationForPayment(getPeriodFormationForPayment(request.getOrderPaymentFormationStartDate()));
                result.add(publicPaymentOptionalDTO);
            }
        });
        return result;
    }
    
    private RequestForPublicReportDTO getFiltersForExportData(Map<String, ?> parameters) {
        var filters = requestForXlsxMapper.toPublicDto(parameters);
        filters.setRequestStatusSet(Set.of(
                TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION));
        if (filters.getDesiredDateRange() == null
            && filters.getCreationDate() == null
            && filters.getApproveDate() == null
            && filters.getOrderPaymentFormationStartDate() == null
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
    
    private Double getMoney(Long paymentPriceMain) {
        if (paymentPriceMain != null && paymentPriceMain > 0) {
            return paymentPriceMain / 100d;
        }
        return null;
    }
    
    private String getPeriodFormationForPayment(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        int dayOfMonth = date.getDayOfMonth();
        
        if (dayOfMonth < 8) {
            return String.valueOf(1);
        }
        if (dayOfMonth < 16) {
            return String.valueOf(2);
        }
        if (dayOfMonth < 24) {
            return String.valueOf(3);
        }
        
        return String.valueOf(4);
    }
}
