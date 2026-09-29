package ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.files.CarsharingReportRegisterDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;
import ru.sberbank.ditsib.transport.reports.service.RequestRegisterService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.ReportConstants.CONFIDENTIAL;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CarsharingReportRegisterResolver implements DataExporter<CarsharingReportRegisterDTO> {
    
    private static final ObjectWriter mapper = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
    private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm:ss";
    private final RequestRegisterService requestRegisterService;
    private final RequestForXlsxMapper requestForXlsxMapper;
    
    @Override
    public String getCaption() {
        return CONFIDENTIAL;
    }
    
    @Override
    public List<CarsharingReportRegisterDTO> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        log.info("Carsharing report export");
        var filters = requestForXlsxMapper.toCarsharingDto(parameters);
        var pageSetting = new RequestReportDTO.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(Integer.MAX_VALUE);
        filters.setPageSetting(pageSetting);
        
        try {
            log.info("Carsharing report export filters {}", mapper.writeValueAsString(filters));
        } catch (Exception e) {
            log.error("Carsharing report export, map to json error", e);
        }
        
        var requests = requestRegisterService.findCarsharingRequests(filters);
        
        var result = new ArrayList<CarsharingReportRegisterDTO>();
        
        requests.forEach(r -> {
            var dto = new CarsharingReportRegisterDTO();
            dto.setHumanReadableId(r.getHumanReadableId());
            dto.setOrganization(r.getOrganization());
            dto.setDepartmentCode(r.getDepartment().getCode());
            dto.setContractor(r.getContractor());
            dto.setRentId(r.getRentId());
            dto.setFio(r.getFio());
            dto.setPersonnelNumber(r.getPersonnelNumber());
            dto.setPosition(r.getPosition());
            dto.setPhoneNumber(r.getPhoneNumber());
            dto.setCreationTime(convertDateTime(r.getCreationTime(), r.getTimeZone()));
            dto.setCar(r.getCar());
            dto.setRentCreatedTime(convertDateTime(r.getRentCreatedTime(), r.getTimeZone()));
            dto.setRentFinishedTime(convertDateTime(r.getRentFinishedTime(), r.getTimeZone()));
            dto.setStartAddress(r.getStartAddress());
            dto.setFinishAddress(r.getFinishAddress());
            dto.setExpectedTime(r.getExpectedTime());
            dto.setReserveTime(r.getReserveTime());
            dto.setDrivingTime(r.getDrivingTime());
            dto.setParkingTime(r.getParkingTime());
            dto.setExpectedDistance(r.getExpectedDistance());
            dto.setDrivingLength(r.getDrivingLength());
            dto.setReserveTimeCost(r.getReserveTimeCost());
            dto.setExpectedCost(r.getExpectedCost());
            dto.setDrivingTimeCost(r.getDrivingTimeCost());
            dto.setParkingTimeCost(r.getParkingTimeCost());
            dto.setDrivingLengthCost(r.getDrivingLengthCost());
            dto.setTotalCost(r.getTotalCost());
            dto.setDesiredDate(convertDateTime(r.getDesiredDate(), r.getTimeZone()));
            dto.setApproveDate(convertDateTime(r.getApproveDate(), r.getTimeZone()));
            dto.setStatus(Optional.ofNullable(r.getStatus()).map(TripRequestStatus::getDescription).orElse(null));
            dto.setTripType(r.isCoopTrip() ? "COOP" : "NOT_COOP");
            dto.setMvz(r.getCostCenter());
            dto.setDepartureAddress(r.getDepartureAddress());
            dto.setIntermediateAddresses(r.getIntermediateAddresses());
            dto.setDestinationAddress(r.getDestinationAddress());
            dto.setPassengerDepartment1(r.getPassengerDepartment1());
            dto.setPassengerDepartment2(r.getPassengerDepartment2());
            dto.setPassengerDepartment3(r.getPassengerDepartment3());
            dto.setPassengerDepartment4(r.getPassengerDepartment4());
            dto.setPassengerDepartment5(r.getPassengerDepartment5());
            dto.setPassengerDepartment6(r.getPassengerDepartment6());
            dto.setPassengerCount(r.getPassengerCount());
            dto.setJoinedPassengers(r.getJoinedPassengers());
            dto.setSource(r.getSource());
            dto.setMinTaxiTariffCost(r.getMinTaxiTariffCost());
            dto.setCommentForPurpose(r.getCommentForPurpose());
            dto.setRequestStatusCode(r.getRequestStatusCode());
            dto.setDeadlineState(r.getDeadlineViolation().equalsIgnoreCase("да") ? "ДА" : "НЕТ");
            dto.setSavings(Boolean.TRUE.equals(r.getSavings()) ? "Есть" : "Нет");
            dto.setContractNumber(r.getContractNumber());
            dto.setRequestClosedDatetime(convertDateTime(r.getRequestClosedDatetime(), r.getTimeZone()));
            dto.setLimitDebit(r.getLimitDebit());
            dto.setDepartmentEconomy(r.getDepartmentEconomy());
            dto.setFinancialImpact(r.getFinancialImpact());
            dto.setFinancialImpactJoined(r.getFinancialImpactJoined());
            dto.setExecutorGroupId(String.valueOf(r.getExecutorGroupId()));
            dto.setExecutorGroupName(r.getExecutorGroupName());
            result.add(dto);
        });
        return result;
    }
    
    private String convertDateTime(LocalDateTime dateTime, String timeZone) {
        if (dateTime != null) {
            ZonedDateTime zdt = dateTime.atZone(ZoneOffset.UTC);
            zdt = zdt.withZoneSameInstant(ZoneId.of(timeZone == null ? "UTC" : timeZone));
            return zdt.format(DateTimeFormatter.ofPattern(CarsharingReportRegisterResolver.DATE_TIME_PATTERN));
        } else {
            return null;
        }
    }
    
}
