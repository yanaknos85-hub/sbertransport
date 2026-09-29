package ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.files.PersonalPaymentDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;
import ru.sberbank.ditsib.transport.reports.messaging.senders.UpdateRequestStatusFromReportsSender;
import ru.sberbank.ditsib.transport.reports.service.RequestService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.PERSONAL_PAYMENT_AWAITING;
import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.CONFIDENTIAL;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PersonalPaymentReport implements DataExporter<PersonalPaymentDTO> {
    
    private static final String RESOURCE_VALUE = "29015";
    private final RequestForXlsxMapper requestForXlsxMapper;
    private final RequestService requestService;
    private final UpdateRequestStatusFromReportsSender statusSender;
    
    @Override
    public String getCaption() {
        return CONFIDENTIAL;
    }
    
    @Override
    public List<PersonalPaymentDTO> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        var result = new ArrayList<PersonalPaymentDTO>();
        var filters = getFiltersForExportData(parameters);
    
        var requests = requestService.findPersonalRequestsForPaymentAndAggregation(filters).stream()
                                     .filter(r -> !r.isCoopTrip() ||
                                                  r.isCoopTrip() &&
                                                  r.getEmployeeDriverId() != null
                                                  && r.getPassenger() != null &&
                                                  r.getPassenger().getId() != null &&
                                                  r.getEmployeeDriverId().equals(r.getPassenger().getId())
                                            )
                                     .toList();
        
        var forSaving = requests.parallelStream().filter(r -> PERSONAL_ORDER_PAYMENT_FORMATION.name().equals(r.getStatus())).toList();
        forSaving.parallelStream().forEach(request -> {
            request.setStatus(PERSONAL_PAYMENT_AWAITING.name());
            request.setOrderPaymentFormationFinishingDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        });
        var saved = requestService.save(forSaving);
        saved.forEach(r -> statusSender.send(r.getId(), r.getStatus(), r.getOrderPaymentFormationFinishingDate(), userId));
        requests.forEach(request -> {
            if (request.getPaymentData() == null) {
                log.error("Request with ID " + request.getId() + " does not have payment data. Request will not be displayed in the download file");
                return;
            }
            var personalPaymentMainDTO = new PersonalPaymentDTO();
            personalPaymentMainDTO.setHumanReadableId(request.getHumanReadableId());
            personalPaymentMainDTO.setPaymentCode(request.getPaymentData().getPaymentTypeCodeMain().getCode());
            personalPaymentMainDTO.setResource(RESOURCE_VALUE);
            personalPaymentMainDTO.setMoney(getMoney(request.getPaymentData().getPaymentPriceMain()));
            personalPaymentMainDTO.setPersonnelNumber(request.getPassenger().getPersonnelNumber());
            personalPaymentMainDTO.setFio(request.getPassenger().getFIO());
            personalPaymentMainDTO.setPeriodFormationForPayment(getPeriodFormationForPayment(request.getOrderPaymentFormationStartDate()));
            result.add(personalPaymentMainDTO);
            if (request.getPaymentData().thereIsInsurancePayment()) {
                var personalPaymentInsuranceDTO = new PersonalPaymentDTO();
                personalPaymentInsuranceDTO.setHumanReadableId(request.getHumanReadableId());
                personalPaymentInsuranceDTO.setPaymentCode(request.getPaymentData().getPaymentTypeCodeInsurance().getCode());
                personalPaymentInsuranceDTO.setResource(RESOURCE_VALUE);
                personalPaymentInsuranceDTO.setMoney(getMoney(request.getPaymentData().getPaymentPriceInsurance()));
                personalPaymentInsuranceDTO.setPersonnelNumber(request.getPassenger().getPersonnelNumber());
                personalPaymentInsuranceDTO.setFio(request.getPassenger().getFIO());
                personalPaymentInsuranceDTO.setPeriodFormationForPayment(getPeriodFormationForPayment(request.getOrderPaymentFormationStartDate()));
                result.add(personalPaymentInsuranceDTO);
            }
            if (request.getPaymentData().thereIsOptionalPayment()) {
                var personalPaymentOptionalDTO = new PersonalPaymentDTO();
                personalPaymentOptionalDTO.setHumanReadableId(request.getHumanReadableId());
                personalPaymentOptionalDTO.setPaymentCode(request.getPaymentData().getPaymentTypeCodeOptional().getCode());
                personalPaymentOptionalDTO.setResource(RESOURCE_VALUE);
                personalPaymentOptionalDTO.setMoney(getMoney(request.getPaymentData().getPaymentPriceOptional()));
                personalPaymentOptionalDTO.setPersonnelNumber(request.getPassenger().getPersonnelNumber());
                personalPaymentOptionalDTO.setFio(request.getPassenger().getFIO());
                personalPaymentOptionalDTO.setPeriodFormationForPayment(getPeriodFormationForPayment(request.getOrderPaymentFormationStartDate()));
                result.add(personalPaymentOptionalDTO);
            }
        });
        return result;
    }
    
    private RequestForPersonalReportDTO getFiltersForExportData(Map<String, ?> parameters) {
        var filters = requestForXlsxMapper.toPersonalDto(parameters);
        
        filters.setRequestStatusSet(Set.of(
                TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION));
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
