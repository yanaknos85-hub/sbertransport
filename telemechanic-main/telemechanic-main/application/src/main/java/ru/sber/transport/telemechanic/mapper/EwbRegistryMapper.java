package ru.sber.transport.telemechanic.mapper;

import lombok.experimental.UtilityClass;
import org.jooq.Converter;
import org.jooq.Record;
import ru.sber.transport.telemechanic.converter.DateToLocalDateConverter;
import ru.sber.transport.telemechanic.converter.StringToEwbStatusConverter;
import ru.sber.transport.telemechanic.converter.TimestampToLocalDateTimeConverter;
import ru.sber.transport.telemechanic.dto.ewb_report.*;
import ru.sber.transport.telemechanic.enumerate.EwbRegistryField;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.helper.EwbRegistryHelper;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static ru.sber.transport.telemechanic.enumerate.EwbRegistryField.*;

@UtilityClass
public class EwbRegistryMapper {
    
    private final TimestampToLocalDateTimeConverter localDateTimeConverter = new TimestampToLocalDateTimeConverter();
    private final DateToLocalDateConverter localDateConverter = new DateToLocalDateConverter();
    private final StringToEwbStatusConverter ewbStatusConverter = new StringToEwbStatusConverter();
    
    public List<EwbRegistryResponse> recordToEwbRegistryResponse(List<Record> source) {
        return source.stream()
                     .map(EwbRegistryMapper::recordToEwbRegistryResponse)
                     .toList();
    }
    
    public List<EwbRegistryExcelAllOrganizationsDto> recordToEwbRegistryExcelAllOrganizationsDto(List<Record> source, Set<EwbRegistryField> fields) {
        return source.stream()
                     .map(rec -> new EwbRegistryExcelAllOrganizationsDto(recordToExcelMap(rec, fields)))
                     .toList();
    }
    
    public List<EwbRegistryExcelSelfOrganizationDto> recordToEwbRegistryExcelSelfOrganizationDto(List<Record> source, Set<EwbRegistryField> fields) {
        return source.stream()
                     .map(rec -> new EwbRegistryExcelSelfOrganizationDto(recordToExcelMap(rec, fields)))
                     .toList();
    }
    
    private EwbRegistryResponse recordToEwbRegistryResponse(Record source) {
        return new EwbRegistryResponse(
                new EwbRegistryInfo(
                        getRequiredFieldValue(source, EWB_HUMAN_READABLE_ID.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, EWB_EWB_UUID.getAlias(), null, null, String.class),
                        getRequiredFieldValue(source, EWB_STATUS.getAlias(), ewbStatusConverter, String.class, EwbStatus.class),
                        getRequiredFieldValue(source, EWB_CREATION_TIME.getAlias(), localDateTimeConverter, Timestamp.class, LocalDateTime.class),
                        getOptionalFieldValue(source, EWB_START_DATE.getAlias(), localDateConverter, Date.class, LocalDate.class),
                        getOptionalFieldValue(source, EWB_FINISH_DATE.getAlias(), localDateConverter, Date.class, LocalDate.class),
                        getRequiredFieldValue(source, EWB_TELEMECH_DECISION_OUT_TIME.getAlias(), localDateTimeConverter, Timestamp.class,
                                              LocalDateTime.class),
                        getRequiredFieldValue(source, EWB_TELEMECH_DECISION_IN_TIME.getAlias(), localDateTimeConverter, Timestamp.class,
                                              LocalDateTime.class),
                        getOptionalFieldValue(source, EWB_MEDIC_DECISION_TIME.getAlias(), localDateTimeConverter, Timestamp.class,
                                              LocalDateTime.class),
                        getOptionalFieldValue(source, EWB_TELEMECH_OUT_MILEAGE.getAlias(), null, null, Integer.class),
                        getOptionalFieldValue(source, EWB_TELEMECH_IN_MILEAGE.getAlias(), null, null, Integer.class),
                        getBooleanFieldValue(source, EWB_MEDIC_SUCCESS.getAlias()),
                        getBooleanFieldValue(source, EWB_TELEMECH_SUCCESS.getAlias())
                ),
                new AuthorRegistryInfo(
                        getOptionalFieldValue(source, AUTHOR_FULL_NAME.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, AUTHOR_PERSONNEL_NUMBER.getAlias(), null, null, String.class)
                ),
                new AttorneyRegistryInfo(
                        getOptionalFieldValue(source, ATTORNEY_NUMBER.getAlias(), null, null, UUID.class),
                        getOptionalFieldValue(source, ATTORNEY_ISSUE_DATE.getAlias(), localDateTimeConverter, Timestamp.class, LocalDateTime.class),
                        getOptionalFieldValue(source, ATTORNEY_CREATION_SYSTEM.getAlias(), null, null, String.class)
                ),
                new DriverRegistryInfo(
                        getRequiredFieldValue(source, DRIVER_FULL_NAME.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, DRIVER_PERSONNEL_NUMBER.getAlias(), null, null, String.class),
                        getRequiredFieldValue(source, DRIVER_ORGANIZATION_NAME.getAlias(), null, null, String.class),
                        getRequiredFieldValue(source, DRIVER_DEPARTMENT_NAME.getAlias(), null, null, String.class)
                ),
                new DrivingLicenseRegistryInfo(
                        getOptionalFieldValue(source, DRIVING_LICENSE_SERIES.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, DRIVING_LICENSE_NUMBER.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, DRIVING_LICENSE_ISSUE_DATE.getAlias(), localDateConverter, Date.class, LocalDate.class),
                        getOptionalFieldValue(source, DRIVING_LICENSE_EXPIRY_DATE.getAlias(), localDateConverter, Date.class, LocalDate.class)
                ),
                new TransportRegistryInfo(
                        getRequiredFieldValue(source, TRANSPORT_STATE_NUMBER.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, TRANSPORT_BRAND.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, TRANSPORT_MODEL.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, TRANSPORT_TYPE_TITLE.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, TRANSPORT_SUBTYPE_TITLE.getAlias(), null, null, String.class)
                ),
                new MedicRegistryInfo(
                        getOptionalFieldValue(source, MEDIC_ORGANIZATION_NAME.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, MEDIC_FULL_NAME.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, MEDIC_PERSONNEL_NUMBER.getAlias(), null, null, String.class)
                ),
                new MedicRequestRegistryInfo(
                        getOptionalFieldValue(source, MEDIC_REQUEST_HUMAN_READABLE_ID.getAlias(), null, null, String.class)
                ),
                new TelemechOutRegistryInfo(
                        getOptionalFieldValue(source, TELEMECH_OUT_ORGANIZATION_NAME.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, TELEMECH_OUT_DEPARTMENT_NAME.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, TELEMECH_OUT_FULL_NAME.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, TELEMECH_OUT_PERSONNEL_NUMBER.getAlias(), null, null, String.class)
                ),
                new RequestRegistryInfo(
                        getOptionalFieldValue(source, REQUEST_HUMAN_READABLE_ID.getAlias(), null, null, String.class)
                ),
                new TelemechInRegistryInfo(
                        getOptionalFieldValue(source, TELEMECH_IN_FULL_NAME.getAlias(), null, null, String.class),
                        getOptionalFieldValue(source, TELEMECH_IN_PERSONNEL_NUMBER.getAlias(), null, null, String.class)
                )
        );
    }
    
    private Map<String, String> recordToExcelMap(Record source, Set<EwbRegistryField> fields) {
        var map = new LinkedHashMap<String, String>();
        for (var field : fields) {
            var fieldValue = EwbRegistryHelper.getFieldValueForExcel(field, source);
            map.put(field.getExcelColumnName(), fieldValue);
        }
        return map;
    }
    
    private <T, U> T getOptionalFieldValue(Record source, String fieldName, Converter<U, T> converter, Class<U> clazzDb, Class<T> clazzReturn) {
        if (converter == null) {
            return source.field(fieldName) != null ? source.get(fieldName, clazzReturn) : null;
        }
        return source.field(fieldName) != null ? converter.from(source.get(fieldName, clazzDb)) : null;
    }
    
    private <T, U> T getRequiredFieldValue(Record source, String fieldName, Converter<U, T> converter, Class<U> clazzDb, Class<T> clazzReturn) {
        if (converter == null) {
            return source.get(fieldName, clazzReturn);
        }
        return converter.from(source.get(fieldName, clazzDb));
    }
    
    private boolean getBooleanFieldValue(Record source, String fieldName) {
        return source.field(fieldName) != null;
    }
}
