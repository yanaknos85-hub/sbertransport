package ru.sber.transport.telemechanic.helper;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.jooq.Field;
import org.jooq.Record;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.sber.transport.telemechanic.enumerate.EwbRegistryField;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EwbRegistryHelperTest {

    @Mock
    Record jooqRecord;

    @Mock
    Field<?> field;

    private static final String MEDIC_REQUEST_STATUS_ALIAS = "medicRequestStatus";

    private static final String REQUEST_STATUS_ALIAS = "requestStatus";

    private static final String TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";

    private static final String DATE_FORMAT = "dd.MM.yyyy";

    private static final String MEDIC_ACCESS = "Прошел предсменный медицинский осмотр, к исполнению трудовых обязанностей допущен";

    private static final String MEDIC_DECLINED = "Не допущен к управлению транспортного средства";

    private static final String TELEMECH_ACCESS = "Выпуск на линию разрешен";

    private static final String TELEMECH_DECLINED = "Доступ ТС на линию запрещен";

    @Test
    void getFieldValueMedicSuccess() {
        doReturn(null).when(jooqRecord).field(MEDIC_REQUEST_STATUS_ALIAS);
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_MEDIC_SUCCESS, jooqRecord));

        doReturn(field).when(jooqRecord).field(MEDIC_REQUEST_STATUS_ALIAS);
        doReturn(null).when(jooqRecord).get(MEDIC_REQUEST_STATUS_ALIAS);
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_MEDIC_SUCCESS, jooqRecord));

        doReturn("IN_PROGRESS").when(jooqRecord).get(MEDIC_REQUEST_STATUS_ALIAS);
        doReturn("IN_PROGRESS").when(jooqRecord).get(MEDIC_REQUEST_STATUS_ALIAS, String.class);
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_MEDIC_SUCCESS, jooqRecord));

        doReturn("DONE").when(jooqRecord).get(MEDIC_REQUEST_STATUS_ALIAS, String.class);
        assertEquals(MEDIC_ACCESS,
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_MEDIC_SUCCESS, jooqRecord));

        doReturn("DECLINED").when(jooqRecord).get(MEDIC_REQUEST_STATUS_ALIAS, String.class);
        assertEquals(MEDIC_DECLINED,
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_MEDIC_SUCCESS, jooqRecord));
    }

    @Test
    void getFieldValueTelemechSuccess() {
        doReturn(null).when(jooqRecord).field(REQUEST_STATUS_ALIAS);
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_SUCCESS, jooqRecord));

        doReturn(field).when(jooqRecord).field(REQUEST_STATUS_ALIAS);
        doReturn(null).when(jooqRecord).get(REQUEST_STATUS_ALIAS);
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_SUCCESS, jooqRecord));

        doReturn("CANCELED").when(jooqRecord).get(REQUEST_STATUS_ALIAS);
        doReturn("CANCELED").when(jooqRecord).get(REQUEST_STATUS_ALIAS, String.class);
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_SUCCESS, jooqRecord));

        doReturn("EXPIRED").when(jooqRecord).get(REQUEST_STATUS_ALIAS, String.class);
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_SUCCESS, jooqRecord));

        doReturn("IN_PROGRESS").when(jooqRecord).get(REQUEST_STATUS_ALIAS, String.class);
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_SUCCESS, jooqRecord));

        doReturn("DONE").when(jooqRecord).get(REQUEST_STATUS_ALIAS, String.class);
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_SUCCESS, jooqRecord));

        doReturn("WARNING").when(jooqRecord).get(REQUEST_STATUS_ALIAS, String.class);
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_SUCCESS, jooqRecord));

        doReturn("DECLINED").when(jooqRecord).get(REQUEST_STATUS_ALIAS, String.class);
        assertEquals(TELEMECH_DECLINED,
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_SUCCESS, jooqRecord));

        doReturn("ON_THE_LINE").when(jooqRecord).get(REQUEST_STATUS_ALIAS, String.class);
        assertEquals(TELEMECH_ACCESS,
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_SUCCESS, jooqRecord));

        doReturn("IN_GARAGE").when(jooqRecord).get(REQUEST_STATUS_ALIAS, String.class);
        assertEquals(TELEMECH_ACCESS,
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_SUCCESS, jooqRecord));

        doReturn("FINISHED").when(jooqRecord).get(REQUEST_STATUS_ALIAS, String.class);
        assertEquals(TELEMECH_ACCESS,
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_SUCCESS, jooqRecord));
    }

    @Test
    void getFieldValueStatus() {
        doReturn("ON_THE_LINE").when(jooqRecord).get(EwbRegistryField.EWB_STATUS.getAlias(), String.class);
        assertEquals(EwbStatus.ON_THE_LINE.getRusName(),
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_STATUS, jooqRecord));
    }

    @Test
    void getFieldValueTimestamp() {
        var dateTimeNow = LocalDateTime.now();

        doReturn(null).when(jooqRecord).field(EwbRegistryField.EWB_CREATION_TIME.getAlias());
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_CREATION_TIME, jooqRecord));

        doReturn(field).when(jooqRecord).field(EwbRegistryField.EWB_CREATION_TIME.getAlias());
        doReturn(null).when(jooqRecord).get(EwbRegistryField.EWB_CREATION_TIME.getAlias());
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_CREATION_TIME, jooqRecord));

        var timestamp = Timestamp.valueOf(dateTimeNow);
        doReturn(timestamp).when(jooqRecord).get(EwbRegistryField.EWB_CREATION_TIME.getAlias());
        doReturn(timestamp).when(jooqRecord).get(EwbRegistryField.EWB_CREATION_TIME.getAlias(), Timestamp.class);
        assertEquals(OffsetDateTime.of(timestamp.toLocalDateTime(), ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of("Europe/Moscow"))
                .format(DateTimeFormatter.ofPattern(TIME_FORMAT)),
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_CREATION_TIME, jooqRecord));

        doReturn(null).when(jooqRecord).field(EwbRegistryField.EWB_TELEMECH_DECISION_OUT_TIME.getAlias());
        assertEquals("",
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_DECISION_OUT_TIME, jooqRecord));

        doReturn(field).when(jooqRecord).field(EwbRegistryField.EWB_TELEMECH_DECISION_OUT_TIME.getAlias());
        doReturn(null).when(jooqRecord).get(EwbRegistryField.EWB_TELEMECH_DECISION_OUT_TIME.getAlias());
        assertEquals("",
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_DECISION_OUT_TIME, jooqRecord));

        doReturn(timestamp).when(jooqRecord).get(EwbRegistryField.EWB_TELEMECH_DECISION_OUT_TIME.getAlias());
        doReturn(timestamp).when(jooqRecord)
            .get(EwbRegistryField.EWB_TELEMECH_DECISION_OUT_TIME.getAlias(), Timestamp.class);
        assertEquals(OffsetDateTime.of(timestamp.toLocalDateTime(), ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of("Europe/Moscow"))
                .format(DateTimeFormatter.ofPattern(TIME_FORMAT)),
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_DECISION_OUT_TIME, jooqRecord));

        doReturn(null).when(jooqRecord).field(EwbRegistryField.EWB_TELEMECH_DECISION_IN_TIME.getAlias());
        assertEquals("",
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_DECISION_IN_TIME, jooqRecord));

        doReturn(field).when(jooqRecord).field(EwbRegistryField.EWB_TELEMECH_DECISION_IN_TIME.getAlias());
        doReturn(null).when(jooqRecord).get(EwbRegistryField.EWB_TELEMECH_DECISION_IN_TIME.getAlias());
        assertEquals("",
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_DECISION_IN_TIME, jooqRecord));

        doReturn(timestamp).when(jooqRecord).get(EwbRegistryField.EWB_TELEMECH_DECISION_IN_TIME.getAlias());
        doReturn(timestamp).when(jooqRecord)
            .get(EwbRegistryField.EWB_TELEMECH_DECISION_IN_TIME.getAlias(), Timestamp.class);
        assertEquals(OffsetDateTime.of(timestamp.toLocalDateTime(), ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of("Europe/Moscow"))
                .format(DateTimeFormatter.ofPattern(TIME_FORMAT)),
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_TELEMECH_DECISION_IN_TIME, jooqRecord));

        doReturn(null).when(jooqRecord).field(EwbRegistryField.EWB_MEDIC_DECISION_TIME.getAlias());
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_MEDIC_DECISION_TIME, jooqRecord));

        doReturn(field).when(jooqRecord).field(EwbRegistryField.EWB_MEDIC_DECISION_TIME.getAlias());
        doReturn(null).when(jooqRecord).get(EwbRegistryField.EWB_MEDIC_DECISION_TIME.getAlias());
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_MEDIC_DECISION_TIME, jooqRecord));

        doReturn(timestamp).when(jooqRecord).get(EwbRegistryField.EWB_MEDIC_DECISION_TIME.getAlias());
        doReturn(timestamp).when(jooqRecord).get(EwbRegistryField.EWB_MEDIC_DECISION_TIME.getAlias(), Timestamp.class);
        assertEquals(OffsetDateTime.of(timestamp.toLocalDateTime(), ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of("Europe/Moscow"))
                .format(DateTimeFormatter.ofPattern(TIME_FORMAT)),
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.EWB_MEDIC_DECISION_TIME, jooqRecord));
    }

    @Test
    void getFieldValueDate() {
        var dateNow = LocalDate.now();

        assertField(EwbRegistryField.EWB_START_DATE, Date.class, Date.valueOf(dateNow),
            dateNow.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        assertField(EwbRegistryField.EWB_FINISH_DATE, Date.class, Date.valueOf(dateNow),
            dateNow.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        assertField(EwbRegistryField.ATTORNEY_ISSUE_DATE, Date.class, Date.valueOf(dateNow),
            dateNow.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
    }

    @Test
    void getFieldValueInteger() {
        assertField(EwbRegistryField.EWB_TELEMECH_OUT_MILEAGE, Integer.class, 100, "100");
        assertField(EwbRegistryField.EWB_TELEMECH_IN_MILEAGE, Integer.class, 150, "150");
    }

    @Test
    void getFieldValueUUID() {
        doReturn(null).when(jooqRecord).field(EwbRegistryField.ATTORNEY_NUMBER.getAlias());
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.ATTORNEY_NUMBER, jooqRecord));

        doReturn(field).when(jooqRecord).field(EwbRegistryField.ATTORNEY_NUMBER.getAlias());
        doReturn(null).when(jooqRecord).get(EwbRegistryField.ATTORNEY_NUMBER.getAlias());
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.ATTORNEY_NUMBER, jooqRecord));

        doReturn(new Object()).when(jooqRecord).get(EwbRegistryField.ATTORNEY_NUMBER.getAlias());
        doReturn(UUID.fromString("920f4f03-75ca-481c-adc5-dff35313b4cf")).when(jooqRecord)
            .get(EwbRegistryField.ATTORNEY_NUMBER.getAlias(), UUID.class);
        assertEquals("920f4f03-75ca-481c-adc5-dff35313b4cf",
            EwbRegistryHelper.getFieldValueForExcel(EwbRegistryField.ATTORNEY_NUMBER, jooqRecord));
    }

    @Test
    void getFieldValueDefault() {
        assertField(EwbRegistryField.EWB_HUMAN_READABLE_ID, String.class, "PL-0001-0000001", "PL-0001-0000001");
        assertField(EwbRegistryField.EWB_EWB_UUID, String.class, "920f4f03-75ca-481c-adc5-dff35313b4cf",
            "920f4f03-75ca-481c-adc5-dff35313b4cf");
        assertField(EwbRegistryField.AUTHOR_FULL_NAME, String.class, "Иванов Иван Ивановчи", "Иванов Иван Ивановчи");
        assertField(EwbRegistryField.AUTHOR_PERSONNEL_NUMBER, String.class, "1112233445", "1112233445");
        assertField(EwbRegistryField.ATTORNEY_CREATION_SYSTEM, String.class, "Система", "Система");
        assertField(EwbRegistryField.DRIVER_FULL_NAME, String.class, "Иванов Иван Ивановчи", "Иванов Иван Ивановчи");
        assertField(EwbRegistryField.DRIVER_PERSONNEL_NUMBER, String.class, "1112233445", "1112233445");
        assertField(EwbRegistryField.DRIVER_ORGANIZATION_NAME, String.class, "ПАО Реча", "ПАО Реча");
        assertField(EwbRegistryField.DRIVER_DEPARTMENT_NAME, String.class, "Группа", "Группа");
        assertField(EwbRegistryField.TRANSPORT_STATE_NUMBER, String.class, "А777АА777", "А777АА777");
        assertField(EwbRegistryField.TRANSPORT_BRAND, String.class, "Mazda", "Mazda");
        assertField(EwbRegistryField.TRANSPORT_MODEL, String.class, "6", "6");
        assertField(EwbRegistryField.MEDIC_ORGANIZATION_NAME, String.class, "ПАО Реча", "ПАО Реча");
        assertField(EwbRegistryField.MEDIC_FULL_NAME, String.class, "Иванов Иван Ивановчи", "Иванов Иван Ивановчи");
        assertField(EwbRegistryField.MEDIC_PERSONNEL_NUMBER, String.class, "1112233445", "1112233445");
        assertField(EwbRegistryField.MEDIC_REQUEST_HUMAN_READABLE_ID, String.class, "TL-0001-0000001",
            "TL-0001-0000001");
        assertField(EwbRegistryField.TELEMECH_OUT_ORGANIZATION_NAME, String.class, "ПАО Реча", "ПАО Реча");
        assertField(EwbRegistryField.TELEMECH_OUT_DEPARTMENT_NAME, String.class, "Группа", "Группа");
        assertField(EwbRegistryField.TELEMECH_OUT_FULL_NAME, String.class, "Иванов Иван Ивановчи",
            "Иванов Иван Ивановчи");
        assertField(EwbRegistryField.TELEMECH_OUT_PERSONNEL_NUMBER, String.class, "1112233445", "1112233445");
        assertField(EwbRegistryField.REQUEST_HUMAN_READABLE_ID, String.class, "TM-0001-0000001", "TM-0001-0000001");
        assertField(EwbRegistryField.TELEMECH_IN_FULL_NAME, String.class, "Иванов Иван Ивановчи",
            "Иванов Иван Ивановчи");
        assertField(EwbRegistryField.TELEMECH_IN_PERSONNEL_NUMBER, String.class, "1112233445", "1112233445");
    }

    private <T> void assertField(EwbRegistryField actualField, Class<T> clazz, T actual, String expected) {
        doReturn(null).when(jooqRecord).field(actualField.getAlias());
        assertEquals("", EwbRegistryHelper.getFieldValueForExcel(actualField, jooqRecord));

        doReturn(field).when(jooqRecord).field(actualField.getAlias());
        doReturn(new Object()).when(jooqRecord).get(actualField.getAlias());
        doReturn(actual).when(jooqRecord).get(actualField.getAlias(), clazz);
        assertEquals(expected, EwbRegistryHelper.getFieldValueForExcel(actualField, jooqRecord));
    }

}
