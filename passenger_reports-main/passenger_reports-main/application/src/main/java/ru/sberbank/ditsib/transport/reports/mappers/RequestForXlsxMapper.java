package ru.sberbank.ditsib.transport.reports.mappers;

import org.slf4j.LoggerFactory;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForGroupTransferReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

public interface RequestForXlsxMapper {
    
    RequestForPersonalReportDTO toPersonalDto(Map<String, ?> parameters);
    
    RequestForPublicReportDTO toPublicDto(Map<String, ?> parameters);
    
    RequestForTaxiReportDTO toTaxiDto(Map<String, ?> parameters);
    
    RequestForCarsharingReportDTO toCarsharingDto(Map<String, ?> parameters);
    
    RequestForGroupTransferReportDTO toGroupTransferDto(Map<String, ?> parameters);
    
    default String mapToString(Object value) {
        if (value == null) {
            return null;
        }
        return (String) value;
    }
    
    default Set<TripPurposeDTO> mapToSetOfTripPurpose(Object value) {
        if (value == null) {
            return Set.of();
        }
        var tripPurposeSet = new HashSet<TripPurposeDTO>();
        var purposesStringArray = mapToString(value).split(";");
        for (var purpose : purposesStringArray) {
            var tripPurposeBuilder = TripPurposeDTO.builder();
            purpose = purpose.replace("[", "").replace("]", "");
            var purposeObjects = purpose.split(",");
            if (purposeObjects.length == 2) {
                tripPurposeBuilder.id(UUID.fromString(purposeObjects[0])).purpose(purposeObjects[1]);
                tripPurposeSet.add(tripPurposeBuilder.build());
            }
            if (purposeObjects.length != 0) {
                LoggerFactory.getLogger(getClass()).warn("Wrong purpose objects: {}. Id and name with comma as delimiter expected.", purpose);
            }
        }
        return tripPurposeSet;
    }
    
    default Set<TripRequestStatus> mapToSetOfTripRequestStatus(Object value) {
        if (value == null) {
            return Set.of();
        }
        var statusSet = new HashSet<TripRequestStatus>();
        var statusStringArray = mapToString(value).split(";");
        for (var string : statusStringArray) {
            statusSet.add(TripRequestStatus.valueOf(string));
        }
        return statusSet;
    }
    
    default Boolean mapToBoolean(Object value) {
        return Optional.ofNullable(value).map(this::mapToString).map(Boolean::valueOf).orElse(null);
    }
    
    default RequestReportDTO.DoubleRange mapToDoubleRange(Object value) {
        if (value == null) {
            return null;
        }
        RequestReportDTO.DoubleRange doubleRange = new RequestReportDTO.DoubleRange();
        var stringArray = mapToString(value).split(";");
        doubleRange.setStart(Double.parseDouble(stringArray[0]));
        doubleRange.setEnd(Double.parseDouble(stringArray[1]));
        return doubleRange;
    }
    
    default RequestReportDTO.DateRange mapToDateRange(Object value) {
        if (value == null) {
            return null;
        }
        RequestReportDTO.DateRange dateRange = new RequestReportDTO.DateRange();
        var stringArray = mapToString(value).split(";");
        dateRange.setStart(mapToLocalDateTime(stringArray[0]));
        dateRange.setEnd(mapToLocalDateTime(stringArray[1]));
        return dateRange;
    }
    
    default LocalDateTime mapToLocalDateTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
    
    default RequestReportDTO.DurationRange mapToDurationRange(Object value) {
        if (value == null) {
            return null;
        }
        RequestReportDTO.DurationRange durationRange = new RequestReportDTO.DurationRange();
        var stringArray = mapToString(value).split(";");
        durationRange.setStart(Duration.ofMillis(Long.parseLong(stringArray[0])));
        durationRange.setEnd(Duration.ofMillis(Long.parseLong(stringArray[1])));
        return durationRange;
    }
    
    default UUID mapToUUID(Object value) {
        if (value == null) {
            return null;
        }
        var string = mapToString(value);
        return UUID.fromString(string);
    }
    
    default Set<PublicTransportType> mapToPublicTransportTypeSet(Object value) {
        if (value == null) {
            return Set.of();
        }
        var typeSet = new HashSet<PublicTransportType>();
        var typeStringArray = mapToString(value).split(";");
        for (var string : typeStringArray) {
            typeSet.add(PublicTransportType.valueOf(string));
        }
        return typeSet;
    }
    
    default Set<PublicCompensationType> mapToPublicCompensationTypeSet(Object value) {
        if (value == null) {
            return Set.of();
        }
        var typeSet = new HashSet<PublicCompensationType>();
        var typeStringArray = mapToString(value).split(";");
        for (var string : typeStringArray) {
            typeSet.add(PublicCompensationType.valueOf(string));
        }
        return typeSet;
    }
    
    default RequestReportDTO.IntegerRange mapToIntegerRange(Object value) {
        if (value == null) {
            return null;
        }
        RequestReportDTO.IntegerRange integerRange = new RequestReportDTO.IntegerRange();
        var stringArray = mapToString(value).split(";");
        integerRange.setStart(Integer.parseInt(stringArray[0]));
        integerRange.setEnd(Integer.parseInt(stringArray[1]));
        return integerRange;
    }
    
    default Set<ItinerantType> mapToEmployeeItinerantTypeSet(Object value) {
        if (value == null) {
            return Set.of();
        }
        var typeSet = new HashSet<ItinerantType>();
        var typeStringArray = mapToString(value).split(";");
        for (var string : typeStringArray) {
            typeSet.add(ItinerantType.valueOf(string));
        }
        return typeSet;
    }
    
    default Set<UUID> mapToUUIDSet(Object value) {
        if (value == null) {
            return Set.of();
        }
        var uuidSet = new HashSet<UUID>();
        var uuidStringArray = mapToString(value).split(";");
        for (var string : uuidStringArray) {
            uuidSet.add(UUID.fromString(string));
        }
        return uuidSet;
    }
    
    default Set<Integer> mapToIntegerSet(Object value) {
        if (value == null) {
            return Set.of();
        }
        var integerSet = new HashSet<Integer>();
        var integerStringArray = mapToString(value).split(";");
        for (var string : integerStringArray) {
            integerSet.add(Integer.valueOf(string));
        }
        return integerSet;
    }
    
    default RequestReportDTO.DateRange getCurrentYear() {
        return new RequestReportDTO.DateRange(
                LocalDateTime.now().with(firstDayOfYear()).toLocalDate().atStartOfDay(),
                LocalDateTime.now().with(lastDayOfYear()).toLocalDate().plusDays(1).atStartOfDay().minusSeconds(1)
        );
    }
}
