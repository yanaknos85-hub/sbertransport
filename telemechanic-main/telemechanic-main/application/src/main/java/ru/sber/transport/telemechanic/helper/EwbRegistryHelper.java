package ru.sber.transport.telemechanic.helper;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.jooq.Record;
import ru.sber.transport.telemechanic.enumerate.EwbRegistryField;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

@UtilityClass
public class EwbRegistryHelper {

    private static final String MEDIC_REQUEST_STATUS_ALIAS = "medicRequestStatus";

    private static final String REQUEST_STATUS_ALIAS = "requestStatus";

    private static final String EMPTY_VALUE = "";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public String getFieldValueForExcel(EwbRegistryField field, Record rec) {
        return switch (field) {
            case EWB_MEDIC_SUCCESS -> getTextForMedicRequest(rec);
            case EWB_TELEMECH_SUCCESS -> getTextForRequest(rec);
            case EWB_STATUS -> EwbStatus.valueOf(rec.get(field.getAlias(), String.class)).getRusName();
            case EWB_CREATION_TIME, EWB_TELEMECH_DECISION_OUT_TIME, EWB_TELEMECH_DECISION_IN_TIME,
                 EWB_MEDIC_DECISION_TIME -> getTextForTimestamp(field, rec);
            case EWB_START_DATE, EWB_FINISH_DATE, DRIVING_LICENSE_ISSUE_DATE, DRIVING_LICENSE_EXPIRY_DATE,
                 ATTORNEY_ISSUE_DATE -> getTextForDate(field, rec);
            case EWB_TELEMECH_OUT_MILEAGE, EWB_TELEMECH_IN_MILEAGE, DRIVING_LICENSE_NUMBER ->
                getTextForInteger(field, rec);
            case ATTORNEY_NUMBER -> getTextForUUID(field, rec);
            default -> rec.field(field.getAlias()) != null ? rec.get(field.getAlias(), String.class) : EMPTY_VALUE;
        };
    }

    private String getTextForMedicRequest(Record rec) {
        if (rec.field(MEDIC_REQUEST_STATUS_ALIAS) == null ||
            rec.get(MEDIC_REQUEST_STATUS_ALIAS) == null ||
            TelemedicineStatus.IN_PROGRESS.equals(
                TelemedicineStatus.valueOf(rec.get(MEDIC_REQUEST_STATUS_ALIAS, String.class)))) {
            return EMPTY_VALUE;
        }

        return TelemedicineStatus.DONE.equals(
            TelemedicineStatus.valueOf(rec.get(MEDIC_REQUEST_STATUS_ALIAS, String.class)))
            ? "Прошел предсменный медицинский осмотр, к исполнению трудовых обязанностей допущен"
            : "Не допущен к управлению транспортного средства";
    }

    private String getTextForRequest(Record rec) {
        if (rec.field(REQUEST_STATUS_ALIAS) == null ||
            rec.get(REQUEST_STATUS_ALIAS) == null ||
            Set.of(
                RequestStatus.CANCELED,
                RequestStatus.EXPIRED,
                RequestStatus.IN_PROGRESS,
                RequestStatus.DONE,
                RequestStatus.WARNING
            ).contains(RequestStatus.valueOf(rec.get(REQUEST_STATUS_ALIAS, String.class)))) {
            return EMPTY_VALUE;
        }

        return RequestStatus.DECLINED.equals(RequestStatus.valueOf(rec.get(REQUEST_STATUS_ALIAS, String.class)))
            ? "Доступ ТС на линию запрещен"
            : "Выпуск на линию разрешен";
    }

    private String getTextForTimestamp(EwbRegistryField field, Record rec) {
        if (rec.field(field.getAlias()) == null || rec.get(field.getAlias()) == null) {
            return EMPTY_VALUE;
        }
        var timestamp = rec.get(field.getAlias(), Timestamp.class);
        var offsetDateTime = OffsetDateTime.of(timestamp.toLocalDateTime(), ZoneOffset.UTC);
        var moscowZoneDateTime = offsetDateTime.atZoneSameInstant(ZoneId.of("Europe/Moscow"));
        return moscowZoneDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    private String getTextForDate(EwbRegistryField field, Record rec) {
        if (rec.field(field.getAlias()) == null || rec.get(field.getAlias()) == null) {
            return EMPTY_VALUE;
        }
        return dateFormat.format(rec.get(field.getAlias(), Date.class));
    }

    private String getTextForInteger(EwbRegistryField field, Record rec) {
        if (rec.field(field.getAlias()) == null || rec.get(field.getAlias()) == null) {
            return EMPTY_VALUE;
        }
        return String.valueOf(rec.get(field.getAlias(), Integer.class));
    }

    private String getTextForUUID(EwbRegistryField field, Record rec) {
        if (rec.field(field.getAlias()) == null || rec.get(field.getAlias()) == null) {
            return EMPTY_VALUE;
        }
        return rec.get(field.getAlias(), UUID.class).toString();
    }

}
