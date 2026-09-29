package ru.sber.transport.telemechanic.mapper;

import lombok.experimental.UtilityClass;
import org.jooq.Converter;
import org.jooq.Record;
import ru.sber.transport.telemechanic.converter.DateToLocalDateConverter;
import ru.sber.transport.telemechanic.converter.StringToMedicRequestStatusConverter;
import ru.sber.transport.telemechanic.converter.TimestampToMillisConverter;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestExcelAllOrganizationsDto;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestExcelSelfOrganizationDto;
import ru.sber.transport.telemechanic.enumerate.MedicRequestField;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;
import ru.sber.transport.telemechanic.helper.MedicRequestRegistryHelper;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;


@UtilityClass
public class MedicRequestRegistryMapper {
    
    private final TimestampToMillisConverter timestampToMillisConverter = new TimestampToMillisConverter();
    private final StringToMedicRequestStatusConverter stringToMedicRequestStatusConverter = new StringToMedicRequestStatusConverter();
    private final DateToLocalDateConverter dateToLocalDateConverter = new DateToLocalDateConverter();
    
    public List<Map<String, Object>> recordToRegistryResponse(List<Record> records, Set<MedicRequestField> columns) {
        return records.stream()
                      .map(jooqRecord -> getRowRequestReport(jooqRecord, columns))
                      .toList();
    }
    
    public List<MedicRequestExcelAllOrganizationsDto> recordToMedicRequestExcelAllOrganizationsDto(List<Record> records,
                                                                                                   Set<MedicRequestField> columns) {
        return records.stream()
                .map(jooqRecord -> new MedicRequestExcelAllOrganizationsDto(recordToExcelMap(jooqRecord, columns)))
                .toList();
    }
    
    public List<MedicRequestExcelSelfOrganizationDto> recordToMedicRequestExcelSelfOrganizationDto(List<Record> records,
                                                                                                   Set<MedicRequestField> columns) {
        return records.stream()
                      .map(jooqRecord -> new MedicRequestExcelSelfOrganizationDto(recordToExcelMap(jooqRecord, columns)))
                      .toList();
    }
    
    private Map<String, Object> getRowRequestReport(Record jooqRecord, Set<MedicRequestField> columns) {
        return columns.stream()
                      .map(column -> new AbstractMap.SimpleEntry<>(column.getAlias(), createResponseObject(column, jooqRecord)))
                      .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
    }
    
    private static Object createResponseObject(MedicRequestField column, Record source) {
        return switch (column) {
            case EWB_MEDIC_DECISION_TIME -> getFieldValue(source, column.getAlias(), timestampToMillisConverter, Timestamp.class, Long.class);
            
            case MEDIC_REQUEST_SYSTOLIC_PRESSURE,
                 MEDIC_REQUEST_DIASTOLIC_PRESSURE,
                 MEDIC_REQUEST_PULSE,
                 DRIVER_LICENSE_NUMBER -> getFieldValue(source, column.getAlias(), null, null, Integer.class);
            
            case MEDIC_REQUEST_TEMPERATURE,
                 MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT -> getFieldValue(source, column.getAlias(), null, null, Double.class);
            
            case MEDIC_REQUEST_STATUS -> getFieldValue(source, column.getAlias(), stringToMedicRequestStatusConverter, String.class,
                                                       TelemedicineStatus.class).getDescription();
            
            case MEDIC_LICENSE_ISSUE_DATE,
                 MEDIC_LICENSE_EXPIRY_DATE,
                 DRIVER_LICENSE_ISSUE_DATE,
                 DRIVER_LICENSE_EXPIRY_DATE -> getFieldValue(source, column.getAlias(), dateToLocalDateConverter, Date.class, LocalDate.class);
            
            default -> getFieldValue(source, column.getAlias(), null, null, String.class);
        };
    }
    
    private <T, U> T getFieldValue(Record source, String fieldName, Converter<U, T> converter, Class<U> clazzDb, Class<T> clazzReturn) {
        if (converter == null) {
            return source.get(fieldName, clazzReturn);
        }
        return converter.from(source.get(fieldName, clazzDb));
    }
    
    private Map<String, String> recordToExcelMap(Record source, Set<MedicRequestField> columns) {
        var map = new HashMap<String, String>();
        for (var column : columns) {
            if (column.equals(MedicRequestField.EWB_ID)) {
                continue;
            }
            var columnName = MedicRequestRegistryHelper.getFieldValueForExcel(column, source);
            map.put(column.getExcelColumnName(), columnName);
        }
        return map;
    }
}
