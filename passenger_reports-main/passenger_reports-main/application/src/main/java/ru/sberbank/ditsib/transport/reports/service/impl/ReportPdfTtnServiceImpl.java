package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.RequestForCargoRepository;
import ru.sberbank.ditsib.transport.reports.exception.ReportWasNotRenderedException;
import ru.sberbank.ditsib.transport.reports.exception.RequestNotFoundException;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.model.cargo.CargoDetail;
import ru.sberbank.ditsib.transport.reports.model.cargo.RequestForCargo;
import ru.sberbank.ditsib.transport.reports.service.ReportPdfTtnService;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class ReportPdfTtnServiceImpl implements ReportPdfTtnService {
    
    @Value("${cargo.template.path}")
    private String reportTemplate;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final int SM = 100;

    private final ResourceLoader resourceLoader;
    
    private final RequestForCargoRepository requestService;
    
    public static final String ORDER_DATE = "orderDate";
    public static final String ORDER_NUMBER = "orderNumber";
    public static final String SENDER = "sender";
    public static final String RECIPIENT = "recipient";
    public static final String SENDER_ORG = "senderOrganization";
    public static final String RECIPIENT_ORG = "recipientOrganization";
    public static final String NAME_CARGO = "nameCargo";
    public static final String NUMBER_CARGO = "numberCargo";
    public static final String WEIGHT_CARGO = "weightCargo";
    public static final String COMMENT = "comment";
    
    @Override
    public byte[] getReport(UUID requestId) {
        try {
            var file = resourceLoader.getResource(reportTemplate);
            var jasperReport = JasperCompileManager.compileReport(file.getInputStream());
            var parameters = fillParameters(requestId);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            log.info("Report TTN pdf created");
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (JRException | IOException e) {
            log.error("Report TTN pdf was not rendered", e);
            throw new ReportWasNotRenderedException(e.getMessage());
        }
    }
    
    public Map<String, Object> fillParameters(UUID requestId) {
        Map<String, Object> parameters = new HashMap<>();
        var request = requestService.findById(requestId).orElseThrow(RequestNotFoundException::new);
        LocalDateTime creationTime = request.getDesiredDate();
        String formatDateTime = creationTime.format(DATE_TIME_FORMATTER);
        parameters.put(ORDER_DATE, formatDateTime);
        parameters.put(ORDER_NUMBER, request.getHumanReadableId());
        parameters.put(SENDER, getInfo(request.getSender()));
        parameters.put(RECIPIENT, getInfo(request.getRecipient()));
        // Грузоотправитель - полное наименование, адрес места нахождения, номер телефона - юридического лица
        parameters.put(SENDER_ORG, getOrganisation(request.getSenderOrganization(), request.getSender()));
        // Грузополучатель - полное наименование, адрес места нахождения, номер телефона - юридического лица
        parameters.put(RECIPIENT_ORG, getOrganisation(request.getRecipientOrganization(), request.getRecipient()));
        // 1-я строка: вид груза, категория груза, название;
        parameters.put(NAME_CARGO, getCategory(request.getCargoDetails()));
        // количество, способ упаковки;
        parameters.put(NUMBER_CARGO, getNumberCargo(request));
        // вес общий в килограммах, размеры (длина, ширина, высота) в метрах, объём в кубических метрах
        parameters.put(WEIGHT_CARGO, getWeightCargo(request));
        // Указания грузоотправителя (дополнительные сведения о грузе, грузчики)
        parameters.put(COMMENT, Objects.nonNull(request.getComment()) ? request.getComment() : "");
        return parameters;
    }
    
    /**
     * 1-я строка: вид груза, категория груза, название
     * @param cargoDetails
     * @return
     */
    private String getCategory(List<CargoDetail> cargoDetails){
        AtomicInteger i = new AtomicInteger();
        cargoDetails.forEach(t->t.setPosition(i.getAndIncrement()));
        String result = cargoDetails.stream().map(CargoDetail::getInfo).collect(Collectors.joining());
        return result;
    }
    
    /**
     * вес общий в килограммах, размеры (длина, ширина, высота) в метрах, объём в кубических метрах
     * @param request
     * @return
     */
    private String getWeightCargo(RequestForCargo request){
        DecimalFormat dF = new DecimalFormat("#.##" );
        var weight = Objects.nonNull(request.getWeight()) ? (request.getWeight().intValue() + "кг  (") : "";
        var length = Objects.nonNull(request.getLength()) ? dF.format(request.getLength()/SM) + "м " : "";
        var height = Objects.nonNull(request.getHeight()) ? dF.format(request.getHeight()/SM) + "м " : "";
        var with = Objects.nonNull(request.getWidth()) ? dF.format(request.getWidth()/SM) + "м " : "";
        var volume = Objects.nonNull(request.getVolume()) ? (dF.format(request.getVolume()/(SM*SM*SM))) + "м3" : "";
        return weight + "(" +length + with + height + ")" + volume;
    }
    
    /**
     * ФИО + телефон
     * @param employee
     * @return
     */
    private String getInfo(Employee employee) {
        return (employee.getLastName() == null ? "" : (employee.getLastName()))+
               (employee.getFirstName() == null ? "" : (" " + employee.getFirstName())) +
               (employee.getPatronymic() == null ? "" : (" " + employee.getPatronymic()))
                 +"  "+ (Objects.nonNull(employee.getMobilePhone()) ? employee.getMobilePhone() : "");
    }
    
    /**
     * Грузоотправитель - полное наименование, адрес места нахождения, номер телефона - юридического лица
     * @param name полное наименование
     * @param employee
     * @return
     */
    private String getOrganisation(String name, Employee employee) {
        var address = "";
        if (Objects.nonNull(employee.getOrganization())
            && Objects.nonNull(employee.getOrganization().getAddress())) {
            address = employee.getOrganization().getAddress();
        }
        return name + " " + address;
    }
    
    private String getNumberCargo(RequestForCargo request) {
        return  Objects.nonNull(request.getOccupiedPlacesCount()) ? request.getOccupiedPlacesCount().toString(): "";
    }
}
