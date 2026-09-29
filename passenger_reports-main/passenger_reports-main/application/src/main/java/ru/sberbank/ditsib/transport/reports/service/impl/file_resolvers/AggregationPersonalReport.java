package ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.files.AggregationPersonalDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.service.RequestService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.CONFIDENTIAL;

@Component
@RequiredArgsConstructor
@Transactional
public class AggregationPersonalReport implements DataExporter<AggregationPersonalDTO> {
    
    private static final String RESOURCE_VALUE = "29015";
    private final RequestForXlsxMapper requestForXlsxMapper;
    private final RequestService requestService;
    
    @Override
    public String getCaption() {
        return CONFIDENTIAL;
    }
    
    @Override
    public List<AggregationPersonalDTO> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        var result = new ArrayList<AggregationPersonalDTO>();
        var filters = getFiltersForExportData(parameters);
    
        var requests = requestService.findPersonalRequestsForPaymentAndAggregation(filters);
        var requestGroupByEmployeeNumber = getMapGroupByEmployeeNumber(requests);
        for (var listByEmployee : requestGroupByEmployeeNumber.entrySet()) {
            var employeeNumber = listByEmployee.getKey();
            var requestsByEmployee = listByEmployee.getValue();
            
            var requestGroupByMainPaymentCode = getMapGroupByMainPaymentCode(requestsByEmployee);
            var requestGroupByOptionalPaymentCode = getMapGroupByOptionalPaymentCode(requestsByEmployee);
            var requestGroupByInsurancePaymentCode = getMapGroupByInsurancePaymentCode(requestsByEmployee);
            var requestGroupByCode =
                    getMapGroupByCode(requestGroupByMainPaymentCode, requestGroupByOptionalPaymentCode, requestGroupByInsurancePaymentCode);
            for (Map.Entry<Integer, List<Request>> listByCode : requestGroupByCode.entrySet()) {
                var paymentCode = listByCode.getKey();
                var sum = getSumPaymentPrice(listByCode);
                var aggregationDTO = new AggregationPersonalDTO();
                aggregationDTO.setPaymentType(paymentCode);
                aggregationDTO.setResource(RESOURCE_VALUE);
                aggregationDTO.setMoney(getMoney(sum));
                aggregationDTO.setPersonnelNumber(employeeNumber);
                aggregationDTO.setFio(requestsByEmployee.get(0).getPassenger().getFIO());
                result.add(aggregationDTO);
            }
        }
        return result;
    }
    
    private RequestForPersonalReportDTO getFiltersForExportData(Map<String, ?> parameters) {
        var filters = requestForXlsxMapper.toPersonalDto(parameters);
        
        filters.setRequestStatusSet(Set.of(
                TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION,
                TripRequestStatus.PERSONAL_PAYMENT_AWAITING));
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
    
    private Map<String, List<Request>> getMapGroupByEmployeeNumber(Collection<Request> requestList) {
        return requestList.stream().collect(Collectors.groupingBy(it -> it.getPassenger().getPersonnelNumber()));
        
    }
    
    private Map<Integer, List<Request>> getMapGroupByOptionalPaymentCode(List<Request> requests) {
        return requests.stream()
                       .filter(it -> it.getPaymentData() != null && it.getPaymentData().getPaymentTypeCodeOptional() != null)
                       .collect(Collectors.groupingBy(it -> it.getPaymentData().getPaymentTypeCodeOptional().getCode()));
    }
    
    private Map<Integer, List<Request>> getMapGroupByMainPaymentCode(List<Request> requests) {
        return requests.stream()
                       .filter(it -> it.getPaymentData() != null && it.getPaymentData().getPaymentTypeCodeMain() != null)
                       .collect(Collectors.groupingBy(it -> it.getPaymentData().getPaymentTypeCodeMain().getCode()));
    }
    
    private Map<Integer, List<Request>> getMapGroupByInsurancePaymentCode(List<Request> requests) {
        return requests.stream()
                       .filter(it -> it.getPaymentData() != null && it.getPaymentData().getPaymentTypeCodeInsurance() != null)
                       .collect(Collectors.groupingBy(it -> it.getPaymentData().getPaymentTypeCodeInsurance().getCode()));
    }
    
    private Map<Integer, List<Request>> getMapGroupByCode(
            Map<Integer, List<Request>> requestsMain, Map<Integer, List<Request>> requestsOptional,
            Map<Integer, List<Request>> requestsInsurance
                                                         ) {
        var paymentTypeCodes = PaymentTypeCode.values();
        var codeMap = new HashMap<Integer, List<Request>>();
        for (var code : paymentTypeCodes) {
            var listByCode = new ArrayList<Request>();
            var mainList = requestsMain.get(code.getCode());
            var optionalList = requestsOptional.get(code.getCode());
            var insuranceList = requestsInsurance.get(code.getCode());
            if (mainList != null) {
                listByCode.addAll(mainList);
            }
            if (optionalList != null) {
                listByCode.addAll(optionalList);
            }
            if (insuranceList != null) {
                listByCode.addAll(insuranceList);
            }
            if (!listByCode.isEmpty()) {
                codeMap.put(code.getCode(), listByCode);
            }
        }
        return codeMap;
    }
    
    private Long getSumPaymentPrice(Map.Entry<Integer, List<Request>> listByCode) {
        var paymentCode = listByCode.getKey();
        var requests = listByCode.getValue();
        long sum = 0;
        for (var request : requests) {
            if (request.getPaymentData() != null) {
                if (PaymentTypeCode.parse(paymentCode).equals(request.getPaymentData().getPaymentTypeCodeMain())) {
                    sum += request.getPaymentData().getPaymentPriceMain();
                }
                if (PaymentTypeCode.parse(paymentCode).equals(request.getPaymentData().getPaymentTypeCodeOptional())) {
                    sum += request.getPaymentData().getPaymentPriceOptional();
                }
                if (PaymentTypeCode.parse(paymentCode).equals(request.getPaymentData().getPaymentTypeCodeInsurance())) {
                    sum += request.getPaymentData().getPaymentPriceInsurance();
                }
            }
        }
        return sum;
    }
    
    private Double getMoney(Long paymentPriceMain) {
        if (paymentPriceMain != null && paymentPriceMain > 0) {
            return paymentPriceMain / 100d;
        }
        return null;
    }
}
