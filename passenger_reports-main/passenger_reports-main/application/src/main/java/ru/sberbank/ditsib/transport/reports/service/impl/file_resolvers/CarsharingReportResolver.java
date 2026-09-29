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
import ru.sberbank.ditsib.transport.reports.dto.TariffShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.files.CarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;
import ru.sberbank.ditsib.transport.reports.service.RequestService;

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
public class CarsharingReportResolver implements DataExporter<CarsharingReportDTO> {
    
    private static final ObjectWriter mapper = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
    private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm:ss";
    private final RequestService requestService;
    private final RequestForXlsxMapper requestForXlsxMapper;
    
    @Override
    public String getCaption() {
        return CONFIDENTIAL;
    }
    
    @Override
    public List<CarsharingReportDTO> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
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
        
        var requests = requestService.findCarsharingRequests(filters);
        
        var result = new ArrayList<CarsharingReportDTO>();
        
        requests.forEach(responseDTO -> {
            var dto = new CarsharingReportDTO();
            dto.setHumanReadableId(responseDTO.getHumanReadableId());
            dto.setOrganization(responseDTO.getOrganization());
            dto.setDepartmentCode(responseDTO.getDepartment().getCode());
            dto.setContractor(responseDTO.getContractor());
            dto.setRentId(responseDTO.getRentId());
            dto.setFio(responseDTO.getFio());
            dto.setPersonnelNumber(responseDTO.getPersonnelNumber());
            dto.setPosition(responseDTO.getPosition());
            dto.setPhoneNumber(responseDTO.getPhoneNumber());
            dto.setCreationTime(convertDateTime(responseDTO.getCreationTime(), responseDTO.getTimeZone(), DATE_TIME_PATTERN));
            dto.setCar(responseDTO.getCar());
            dto.setRentCreatedTime(convertDateTime(responseDTO.getRentCreatedTime(), responseDTO.getTimeZone(), DATE_TIME_PATTERN));
            dto.setRentFinishedTime(convertDateTime(responseDTO.getRentFinishedTime(), responseDTO.getTimeZone(), DATE_TIME_PATTERN));
            dto.setStartAddress(responseDTO.getStartAddress());
            dto.setFinishAddress(responseDTO.getFinishAddress());
            dto.setExpectedTime(responseDTO.getExpectedTime());
            dto.setReserveTime(responseDTO.getReserveTime());
            dto.setDrivingTime(responseDTO.getDrivingTime());
            dto.setParkingTime(responseDTO.getParkingTime());
            dto.setExpectedDistance(responseDTO.getExpectedDistance());
            dto.setDrivingLength(responseDTO.getDrivingLength());
            dto.setReserveTimeCost(responseDTO.getReserveTimeCost());
            dto.setExpectedCost(responseDTO.getExpectedCost());
            dto.setDrivingTimeCost(responseDTO.getDrivingTimeCost());
            dto.setParkingTimeCost(responseDTO.getParkingTimeCost());
            dto.setDrivingLengthCost(responseDTO.getDrivingLengthCost());
            dto.setTotalCost(responseDTO.getTotalCost());
            dto.setDesiredDate(convertDateTime(responseDTO.getDesiredDate(), responseDTO.getTimeZone(), DATE_TIME_PATTERN));
            dto.setApproveDate(convertDateTime(responseDTO.getApproveDate(), responseDTO.getTimeZone(), DATE_TIME_PATTERN));
            dto.setStatus(Optional.ofNullable(responseDTO.getStatus()).map(TripRequestStatus::getDescription).orElse(null));
            dto.setTripType(responseDTO.isCoopTrip() ? "COOP" : "NOT_COOP");
            dto.setMvz(responseDTO.getCostCenter());
            dto.setDepartureAddress(responseDTO.getDepartureAddress());
            dto.setIntermediateAddresses(responseDTO.getIntermediateAddresses());
            dto.setDestinationAddress(responseDTO.getDestinationAddress());
            dto.setPassengerDepartment1(responseDTO.getPassengerDepartment1());
            dto.setPassengerDepartment2(responseDTO.getPassengerDepartment2());
            dto.setPassengerDepartment3(responseDTO.getPassengerDepartment3());
            dto.setPassengerDepartment4(responseDTO.getPassengerDepartment4());
            dto.setPassengerDepartment5(responseDTO.getPassengerDepartment5());
            dto.setPassengerDepartment6(responseDTO.getPassengerDepartment6());
            dto.setPassengerCount(responseDTO.getPassengerCount());
            dto.setJoinedPassengers(responseDTO.getJoinedPassengers());
            dto.setSource(responseDTO.getSource());
            dto.setMinTaxiTariffCost(responseDTO.getMinTaxiTariffCost());
            dto.setCommentForPurpose(responseDTO.getCommentForPurpose());
            dto.setRequestStatusCode(responseDTO.getRequestStatusCode());
            dto.setDeadlineState(responseDTO.getDeadlineViolation().equalsIgnoreCase("да") ? "ДА" : "НЕТ");
            dto.setSavings(Boolean.TRUE.equals(responseDTO.getSavings()) ? "Есть" : "Нет");
            dto.setContractNumber(responseDTO.getContractNumber());
            dto.setRequestClosedDatetime(convertDateTime(responseDTO.getRequestClosedDatetime(), responseDTO.getTimeZone(), DATE_TIME_PATTERN));
            dto.setLimitDebit(responseDTO.getLimitDebit());
            dto.setDepartmentEconomy(responseDTO.getDepartmentEconomy());
            dto.setFinancialImpact(responseDTO.getFinancialImpact());
            dto.setFinancialImpactJoined(responseDTO.getFinancialImpactJoined());
            dto.setExecutorGroupId(String.valueOf(responseDTO.getExecutorGroupId()));
            dto.setExecutorGroupName(responseDTO.getExecutorGroupName());
            dto.setTariffName(Optional.ofNullable(responseDTO.getTariff()).map(TariffShortDTO::getHumanReadableId).orElse(null));
            result.add(dto);
        });
        return result;
    }
    
    private String convertDateTime(LocalDateTime dateTime, String timeZone, String pattern) {
        if (dateTime != null) {
            ZonedDateTime zdt = dateTime.atZone(ZoneOffset.UTC);
            zdt = zdt.withZoneSameInstant(ZoneId.of(timeZone == null ? "UTC" : timeZone));
            return zdt.format(DateTimeFormatter.ofPattern(pattern));
        } else {
            return null;
        }
    }
    
}
