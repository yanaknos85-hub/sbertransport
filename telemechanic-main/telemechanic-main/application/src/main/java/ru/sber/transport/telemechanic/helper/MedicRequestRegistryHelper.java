package ru.sber.transport.telemechanic.helper;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;
import org.jooq.Record;
import ru.sber.transport.telemechanic.enumerate.MedicRequestField;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

@UtilityClass
public class MedicRequestRegistryHelper {

    private static final String EMPTY_VALUE = "";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public String getFieldValueForExcel(MedicRequestField field, Record jooqRecord) {
        if (jooqRecord.get(field.getAlias()) == null) {
            return EMPTY_VALUE;
        }
        return switch (field) {
            case EWB_MEDIC_DECISION_TIME -> getTextFromTimestamp(field, jooqRecord);
            case MEDIC_REQUEST_SYSTOLIC_PRESSURE,
                 MEDIC_REQUEST_DIASTOLIC_PRESSURE,
                 MEDIC_REQUEST_PULSE,
                 DRIVER_LICENSE_NUMBER -> getTextFromInteger(field, jooqRecord);
            case MEDIC_REQUEST_TEMPERATURE,
                 MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT -> getTextFromDouble(field, jooqRecord);
            case MEDIC_REQUEST_STATUS -> getTextFromStatus(field, jooqRecord);
            case MEDIC_LICENSE_ISSUE_DATE,
                 MEDIC_LICENSE_EXPIRY_DATE,
                 DRIVER_LICENSE_ISSUE_DATE,
                 DRIVER_LICENSE_EXPIRY_DATE -> getTextFromDate(field, jooqRecord);

            default -> getTextFromString(field, jooqRecord);
        };
    }

    private String getTextFromTimestamp(MedicRequestField field, Record jooqRecord) {
        if (jooqRecord.field(field.getAlias()) == null || jooqRecord.get(field.getAlias()) == null) {
            return EMPTY_VALUE;
        }
        var timestamp = jooqRecord.get(field.getAlias(), Timestamp.class);
        var offsetDateTime = OffsetDateTime.of(timestamp.toLocalDateTime(), ZoneOffset.UTC);
        var moscowZoneDateTime = offsetDateTime.atZoneSameInstant(ZoneId.of("Europe/Moscow"));
        return moscowZoneDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    private String getTextFromInteger(MedicRequestField field, Record jooqRecord) {
        return jooqRecord.get(field.getAlias(), Integer.class).toString();
    }

    private String getTextFromDouble(MedicRequestField field, Record jooqRecord) {
        return jooqRecord.get(field.getAlias(), Double.class).toString();
    }

    private String getTextFromStatus(MedicRequestField field, Record jooqRecord) {
        return TelemedicineStatus.valueOf(jooqRecord.get(field.getAlias(), String.class)).getDescription();
    }

    private String getTextFromDate(MedicRequestField field, Record jooqRecord) {
        return dateFormat.format(jooqRecord.get(field.getAlias(), Date.class));
    }

    private String getTextFromString(MedicRequestField field, Record jooqRecord) {
        return jooqRecord.get(field.getAlias(), String.class);
    }

}
