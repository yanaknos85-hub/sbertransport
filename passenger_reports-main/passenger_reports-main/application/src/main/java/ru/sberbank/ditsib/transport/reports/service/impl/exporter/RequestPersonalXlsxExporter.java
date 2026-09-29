package ru.sberbank.ditsib.transport.reports.service.impl.exporter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.PersonalUIVisibilityDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryCheckDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.messaging.senders.UpdateRequestStatusFromReportsSender;
import ru.sberbank.ditsib.transport.reports.model.PaymentData;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.service.*;
import ru.sberbank.ditsib.transport.reports.service.impl.PaymentColumnNames;
import ru.sberbank.ditsib.transport.reports.utils.Function4;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.PERSONAL;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.PERSONAL_PAYMENT_AWAITING;

@RequiredArgsConstructor
@Slf4j
@Service
class RequestPersonalXlsxExporter implements XlsxExporter {
    
    private static final String FOUND_TRIP_COUNT_MESSAGE = "Количество найденных поездок: ";
    private static final String RESOURCE_VALUE = "29015";
    
    static final String NA = "Н/Д";
    private final ReportsSpecService<RequestForPersonalReportDTO> personalSpecService;
    private final RequestService requestService;
    private final DepartmentService departmentService;
    private final UpdateRequestStatusFromReportsSender statusSender;
    private final XlsFormer former;
    private final Mappings<PersonalUIVisibilityDTO> mapping;
    
    @Override
    public TransportTypeEnum transportType() {
        return PERSONAL;
    }
    
    @Override
    @Async
    @Transactional
    public void exportToPaymentXlsx(String filename, RequestReportDTO requestDTO, UUID userId) {
        try {
            log.info("Start exporting payment report: {}", filename);
            former.writeStarted(filename);
            var spec = personalSpecService.getReportSpec((RequestForPersonalReportDTO) requestDTO);
            var requests = requestService.findAllBySpec(spec);
    
            log.info(FOUND_TRIP_COUNT_MESSAGE + requests.size());
            var forSaving = requests.stream().filter(r -> PERSONAL_ORDER_PAYMENT_FORMATION.name().equals(r.getStatus())).toList();
            forSaving.forEach(request -> {
                request.setStatus(PERSONAL_PAYMENT_AWAITING.name());
                request.setOrderPaymentFormationFinishingDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
            });
            var saved = requestService.save(forSaving);
            saved.forEach(r -> statusSender.send(r.getId(), r.getStatus(), r.getOrderPaymentFormationFinishingDate(), userId));
    
            log.debug("Found {} requests", saved.size());
            exportToPaymentXlsx(filename, requests, filename);
        } catch (Exception e) {
            former.writeError(e, filename);
        }
    }
    
    /**
     * Создание xlsx из набора request, с полями заданными в mapping
     *
     * @param records коллекция заявок
     * @param mapping коллекция полей и их маппинг для отображения
     */
    private void exportToXlsx(String fileName,
            Collection<Request> records,
            Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> mapping
                               ) {
        Table<Integer, String, Object> table = HashBasedTable.create();
        var rowIndex = new AtomicInteger(0);
        var row = rowIndex.get();
        if (records.isEmpty()) {
            mapping.forEach((column, provider) -> table.put(rowIndex.get(), column, ""));
        }
        log.debug("Creating requests table for {}", transportType());
        for (var item : records) {
            log.debug("Add {} row to table", row);
            for (var entry : mapping.entrySet()) {
                var column = entry.getKey();
                log.debug("Add {} row {} column to table", row, column);
                var provider = entry.getValue();
            
                var applyValue = Optional.ofNullable(provider.apply(item, null, null)).orElse("");
                table.put(row, column, applyValue);
            }
            row = rowIndex.incrementAndGet();
        }
        log.debug("Creating requests table for {} finished. {} rows added", transportType(), row);
        former.exportToXlsx(fileName, table, "Отчет по поездкам", transportType());
    }
    
    private void exportToPaymentXlsx(String fileName, Collection<Request> requestList, String sheetName) {
        Table<Integer, String, Object> table = HashBasedTable.create();
        var rowIndex = 0;
        
        log.debug("Creating table");
        for (var request : requestList) {
            var periodFormationForPayment = getPeriodFormationForPayment(request.getOrderPaymentFormationStartDate());
            var paymentData = request.getPaymentData();
            if (paymentData == null) {
                continue;
            }
            
            table.put(rowIndex, PaymentColumnNames.HUMAN_READABLE_ID, request.getHumanReadableId());
            table.put(rowIndex, PaymentColumnNames.PAYMENT_TYPE, String.valueOf(paymentData.getPaymentTypeCodeMain().getCode()));
            table.put(rowIndex, PaymentColumnNames.RESOURCE, RESOURCE_VALUE);
            table.put(rowIndex, PaymentColumnNames.EMPLOYEE_NUMBER, String.valueOf(request.getPassenger().getPersonnelNumber()));
            table.put(rowIndex, PaymentColumnNames.MONEY, getMoney(paymentData.getPaymentPriceMain()));
            table.put(rowIndex, PaymentColumnNames.EMPLOYEE_NAME, request.getPassenger().getFIO());
            table.put(rowIndex, PaymentColumnNames.PERIOD_FORMATION_ORDER,
                      Optional.ofNullable(periodFormationForPayment).map(String::valueOf).orElse(""));
            
            if (paymentData.thereIsOptionalPayment()) {
                rowIndex += 1;
                table.put(rowIndex, PaymentColumnNames.HUMAN_READABLE_ID, request.getHumanReadableId());
                table.put(rowIndex, PaymentColumnNames.PAYMENT_TYPE, String.valueOf(paymentData.getPaymentTypeCodeOptional().getCode()));
                table.put(rowIndex, PaymentColumnNames.RESOURCE, RESOURCE_VALUE);
                table.put(rowIndex, PaymentColumnNames.EMPLOYEE_NUMBER, String.valueOf(request.getPassenger().getPersonnelNumber()));
                table.put(rowIndex, PaymentColumnNames.MONEY, getMoney(paymentData.getPaymentPriceOptional()));
                table.put(rowIndex, PaymentColumnNames.EMPLOYEE_NAME, request.getPassenger().getFIO());
                table.put(rowIndex, PaymentColumnNames.PERIOD_FORMATION_ORDER,
                          Optional.ofNullable(periodFormationForPayment).map(String::valueOf).orElse(""));
            }
            rowIndex += 1;
        }
        
        log.debug("Creating file");
        fileName = former.createTemporaryFile(fileName);
        try (var workbook = new XSSFWorkbook();
             var fos = new FileOutputStream(fileName)) {
            
            log.debug("Adding sheet: {}", sheetName);
            former.addSheet(workbook, table, sheetName, transportType());
            var aggregationTable = getAggregationTable(requestList);
            
            sheetName = sheetName.replace("Реестр_поездок", "Агрегированный");
    
            log.debug("Adding sheet: {}", sheetName);
            former.addSheet(workbook, aggregationTable, sheetName, transportType());
    
            log.debug("Writing file {}", fileName);
            workbook.write(fos);
            former.writeSuccess(fileName);
            log.info("File {} written", fileName);
        } catch (IOException e) {
            former.writeError(e, fileName);
            log.warn("File {} failed", fileName);
        }
    }
    
    @Override
    @Async
    @Transactional
    public void exportToXlsx(String filename, String token, RequestReportDTO requestReportDTO) {
        try {
            log.info("Start exporting file {}", filename);
            former.writeStarted(filename);
            var spec = personalSpecService.getReportSpec((RequestForPersonalReportDTO) requestReportDTO);
            var requestsMap = requestService.findAllBySpec(spec).stream().collect(Collectors.toMap(Request::getId, Function.identity()));
            departmentService.setDepartmentHierarchy(requestsMap);
            var requests = requestsMap.values();
    
            log.info(FOUND_TRIP_COUNT_MESSAGE + requests.size());
            
            log.debug("Forming {} requests to file", requestsMap.keySet().size());
    
            exportToXlsx(filename, requests, mapping.get(((RequestForPersonalReportDTO) requestReportDTO).getPersonalUIVisibilityDTO()));
            log.info("Exporting file {} succeeded", filename);
        } catch (Exception e) {
            former.writeError(e, filename);
            log.warn("Exporting file {} failed", filename);
        }
    }
    
    private Integer getPeriodFormationForPayment(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        int dayOfMonth = date.getDayOfMonth();
        
        if (dayOfMonth < 8) {
            return 1;
        }
        if (dayOfMonth < 16) {
            return 2;
        }
        if (dayOfMonth < 24) {
            return 3;
        }
        
        return 4;
    }
    
    private Table<Integer, String, Object> getAggregationTable(Collection<Request> requestList) {
        var requestGroupByEmployeeNumber = getMapGroupByEmployeeNumber(requestList);
        
        Table<Integer, String, Object> table = HashBasedTable.create();
        var rowIndex = new AtomicInteger(0);
        
        for (var listByEmployee : requestGroupByEmployeeNumber.entrySet()) {
            var employeeNumber = listByEmployee.getKey();
            var requestsByEmployee = listByEmployee.getValue();
            
            var requestGroupByMainPaymentCode = getMapGroupByMainPaymentCode(requestsByEmployee);
            var requestGroupByOptionalPaymentCode = getMapGroupByOptionalPaymentCode(requestsByEmployee);
            var requestGroupByCode = getMapGroupByCode(requestGroupByMainPaymentCode, requestGroupByOptionalPaymentCode);
            for (Map.Entry<Integer, List<Request>> listByCode : requestGroupByCode.entrySet()) {
                var paymentCode = listByCode.getKey();
                var sum = getSumPaymentPrice(listByCode);
                table.put(rowIndex.get(), PaymentColumnNames.PAYMENT_TYPE, String.valueOf(paymentCode));
                table.put(rowIndex.get(), PaymentColumnNames.RESOURCE, RESOURCE_VALUE);
                table.put(rowIndex.get(), PaymentColumnNames.EMPLOYEE_NUMBER, String.valueOf(employeeNumber));
                table.put(rowIndex.get(), PaymentColumnNames.MONEY, getMoney(sum));
                table.put(rowIndex.get(), PaymentColumnNames.EMPLOYEE_NAME, requestsByEmployee.get(0).getPassenger().getFIO());
                rowIndex.incrementAndGet();
            }
        }
        return table;
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
    
    private Map<Integer, List<Request>> getMapGroupByCode(Map<Integer, List<Request>> requestsMain, Map<Integer, List<Request>> requestsOptional) {
        var paymentTypeCodes = PaymentTypeCode.values();
        var codeMap = new HashMap<Integer, List<Request>>();
        for (var code : paymentTypeCodes) {
            var listByCode = new ArrayList<Request>();
            var mainList = requestsMain.get(code.getCode());
            var optionalList = requestsOptional.get(code.getCode());
            if(mainList!=null) {
                listByCode.addAll(mainList);
            }
            if(optionalList!=null) {
                listByCode.addAll(optionalList);
            }
            if(!listByCode.isEmpty()) {
                codeMap.put(code.getCode(), listByCode);
            }
        }
        return codeMap;
    }
    
    private Long getSumPaymentPriceOptional(List<Request> requests) {
        return requests.stream()
                       .map(Request::getPaymentData)
                       .filter(paymentData -> paymentData.getPaymentPriceOptional()!=null)
                       .mapToLong(PaymentData::getPaymentPriceOptional)
                       .sum();
    }
    
    private Long getSumPaymentPriceMain(List<Request> requests) {
        return requests.stream()
                       .map(Request::getPaymentData)
                       .filter(paymentData -> paymentData.getPaymentPriceMain()!=null)
                       .mapToLong(PaymentData::getPaymentPriceMain)
                       .sum();
    }
    
    private Long getSumPaymentPrice(Map.Entry<Integer, List<Request>> listByCode){
        var paymentCode = listByCode.getKey();
        var requests = listByCode.getValue();
        long sum = 0;
        for (var request : requests) {
            if(request.getPaymentData()!=null){
                if(PaymentTypeCode.parse(paymentCode).equals(request.getPaymentData().getPaymentTypeCodeMain())){
                    sum += request.getPaymentData().getPaymentPriceMain();
                }
                if(PaymentTypeCode.parse(paymentCode).equals(request.getPaymentData().getPaymentTypeCodeOptional())){
                    sum += request.getPaymentData().getPaymentPriceOptional();
                }
            }
        }
        return sum;
    }
    
    private Map<String, List<Request>> getMapGroupByEmployeeNumber(Collection<Request> requestList) {
        return requestList.stream().collect(Collectors.groupingBy(it -> it.getPassenger().getPersonnelNumber()));
        
    }
    
    private Object getMoney(Long paymentPriceMain) {
        if (paymentPriceMain != null && paymentPriceMain > 0) {
            return paymentPriceMain / 100d;
        }
        return NA;
    }
    
}