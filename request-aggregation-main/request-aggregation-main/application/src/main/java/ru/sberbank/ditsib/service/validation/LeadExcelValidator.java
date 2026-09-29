package ru.sberbank.ditsib.service.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.dto.file.FieldWithValidation;
import ru.sberbank.ditsib.dto.lead.ValidatedLeadDto;
import ru.sberbank.ditsib.dto.file.ValidateFileResponseDto;
import ru.sberbank.ditsib.dto.lead.LeadExcelDto;
import ru.sberbank.ditsib.enumerate.TransportClass;
import ru.sberbank.ditsib.enumerate.TransportType;
import ru.sberbank.ditsib.enumerate.TripType;
import ru.sberbank.ditsib.service.GeoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LeadExcelValidator {

    private static final String MISSING_REQUIRED_FIELD = "Поле не заполнено";
    private final GeoService geoService;
    private final EmployeeRepository employeeRepository;

    public ValidateFileResponseDto validate(List<LeadExcelDto> dtos, int departureLag) {
        var leads = dtos.stream()
                .filter(Objects::nonNull)
                .map(lead -> {
                    var validatedDto = ValidatedLeadDto.builder()
                            .transportClass(validateTransportClass(lead.getTransportClass()))
                            .transportType(validateTransportType(lead.getTransportType()))
                            .tripType(validateTripType(lead.getTripType()))
                            .build();
                    validateEmployee(lead, validatedDto);
                    validateAddressFrom(lead.getAddressFrom(), validatedDto);
                    validateAddressTo(lead.getAddressTo(), validatedDto);
                    validateDeparture(lead.getOrderDate(), lead.getOrderTime(), validatedDto, departureLag);
                    return validatedDto;
                })
                .toList();
        return new ValidateFileResponseDto(leads);
    }

    private void validateDeparture(LocalDate date, LocalTime time, ValidatedLeadDto lead, int departureLag) {
        if (date == null && time == null) {
            lead.setOrderDate(new FieldWithValidation<>(date, true, MISSING_REQUIRED_FIELD));
            lead.setOrderTime(new FieldWithValidation<>(time, true, MISSING_REQUIRED_FIELD));
            return;
        }
        if (date != null && time == null) {
            lead.setOrderDate(new FieldWithValidation<>(date, false, null));
            lead.setOrderTime(new FieldWithValidation<>(time, true, MISSING_REQUIRED_FIELD));
            return;
        }
        if (date == null) {
            lead.setOrderDate(new FieldWithValidation<>(date, true, MISSING_REQUIRED_FIELD));
            lead.setOrderTime(new FieldWithValidation<>(time, false, null));
            return;
        }
        var withLag = LocalDateTime.now().plusHours(departureLag);
        if (withLag.isAfter(LocalDateTime.of(date, time))) {
            lead.setOrderDate(new FieldWithValidation<>(date, true,
                    "Дата и время заказа должны быть с учетом задержки в "
                            + departureLag + " часа от текущего времени"));
            lead.setOrderTime(new FieldWithValidation<>(time, true,
                    "Дата и время заказа должны быть с учетом задержки в "
                            + departureLag + " часа от текущего времени"));
        } else {
            lead.setOrderDate(new FieldWithValidation<>(date, false, null));
            lead.setOrderTime(new FieldWithValidation<>(time, false, null));
        }
    }

    private FieldWithValidation<String> validateTransportType(String transportType) {
        if (transportType == null || transportType.isBlank()) {
            return new FieldWithValidation<>(transportType, true, MISSING_REQUIRED_FIELD);
        }
        if (TransportType.getTransportType(transportType).isEmpty()) {
            return new FieldWithValidation<>(transportType, true, "Введенный вид транспорта не существует");
        }
        return new FieldWithValidation<>(transportType, false, null);
    }

    private FieldWithValidation<String> validateTransportClass(String transportClass) {
        if (transportClass == null || transportClass.isBlank()) {
            return new FieldWithValidation<>(transportClass, true, MISSING_REQUIRED_FIELD);

        }
        if (TransportClass.getTransportClass(transportClass).isEmpty()) {
            return new FieldWithValidation<>(transportClass, true, "Введенный тип транспорта не существует");
        }
        return new FieldWithValidation<>(transportClass, false, null);
    }

    private FieldWithValidation<String> validateTripType(String tripType) {
        if (tripType == null || tripType.isBlank()) {
            return new FieldWithValidation<>(tripType, true, MISSING_REQUIRED_FIELD);
        }
        if (TripType.getTripType(tripType).isEmpty()) {
            return new FieldWithValidation<>(tripType, true, "Введенная цель поездки не существует");
        }
        return new FieldWithValidation<>(tripType, false, null);
    }

    private void validateEmployee(LeadExcelDto dto, ValidatedLeadDto lead) {
        if (dto.getPersonnelNumber() == null || dto.getPersonnelNumber().isBlank()) {
            lead.setPersonnelNumber(new FieldWithValidation<>(dto.getPersonnelNumber(), true, MISSING_REQUIRED_FIELD));
        }
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            lead.setFullName(new FieldWithValidation<>(dto.getFullName(), true, MISSING_REQUIRED_FIELD));
        }
        if (lead.getPersonnelNumber() == null && lead.getFullName() == null) {
            var employeeOpt = employeeRepository.findByPersonnelNumber(dto.getPersonnelNumber());
            if (employeeOpt.isEmpty()) {
                lead.setPersonnelNumber(new FieldWithValidation<>(dto.getPersonnelNumber(), true, "Табельный номер не существует"));
                return;
            }
            var employee = employeeOpt.get();
            if (!employee.getFIO().equals(dto.getFullName())) {
                lead.setFullName(new FieldWithValidation<>(dto.getFullName(), true, "ФИО не совпадает с табельным номером"));
                return;
            }
            lead.setPersonnelNumber(new FieldWithValidation<>(dto.getPersonnelNumber(), false, null));
            lead.setFullName(new FieldWithValidation<>(dto.getFullName(), false, null));
        }
    }

    private void validateAddressFrom(String address, ValidatedLeadDto lead) {
        if (address == null || address.isBlank()) {
            lead.setAddressFrom(new FieldWithValidation<>(address, true, MISSING_REQUIRED_FIELD));
            return;
        }
        if (address.length() > 255) {
            lead.setAddressFrom(new FieldWithValidation<>(address, true, "Введенный адрес слишком длинный"));
            return;
        }
        var addresses = geoService.getGeoAddress(address);
        if (!addresses.isEmpty()) {
            var geoAddress = addresses.get(0);
            lead.setAddressFrom(new FieldWithValidation<>(address, false, null));
            lead.setAddressFromLatitude(geoAddress.latitude().doubleValue());
            lead.setAddressFromLongitude(geoAddress.longitude().doubleValue());
            return;
        }
        lead.setAddressFrom(new FieldWithValidation<>(address, true, "Данный адрес не существует"));
    }

    private void validateAddressTo(String address, ValidatedLeadDto lead) {
        if (address == null || address.isBlank()) {
            lead.setAddressTo(new FieldWithValidation<>(address, true, MISSING_REQUIRED_FIELD));
            return;
        }
        if (address.length() > 255) {
            lead.setAddressTo(new FieldWithValidation<>(address, true, "Введенный адрес слишком длинный"));
            return;
        }
        var addresses = geoService.getGeoAddress(address);
        if (!addresses.isEmpty()) {
            var geoAddress = addresses.get(0);
            lead.setAddressTo(new FieldWithValidation<>(address, false, null));
            lead.setAddressToLatitude(geoAddress.latitude().doubleValue());
            lead.setAddressToLongitude(geoAddress.longitude().doubleValue());
            return;
        }
        lead.setAddressTo(new FieldWithValidation<>(address, true, "Данный адрес не существует"));
    }
}
