package ru.sber.transport.telemechanic.mapper;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.jooq.Field;
import org.jooq.Record;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.dto.ewb_report.*;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static ru.sber.transport.telemechanic.enumerate.EwbRegistryField.*;

@ExtendWith(MockitoExtension.class)
class EwbRegistryMapperTest {

    @Mock
    private Record record;

    @Mock
    private Field field;

    private static final LocalDateTime NOW = LocalDateTime.now(ZoneOffset.UTC);

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Test
    void shouldMapJooqRecordToEwbRegistryResponse() {
        var source = List.of(record);

        when(record.get(EWB_HUMAN_READABLE_ID.getAlias(), String.class)).thenReturn("PL-0001-0000001");
        when(record.field(anyString())).thenReturn(field);
        when(record.get(EWB_EWB_UUID.getAlias(), String.class)).thenReturn("49e3d6e4-c650-48ff-b827-143fda6a4d06");
        when(record.get(EWB_STATUS.getAlias(), String.class)).thenReturn(EwbStatus.ON_THE_LINE.name());
        when(record.get(EWB_CREATION_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_START_DATE.getAlias(), Date.class)).thenReturn(Date.valueOf(NOW.toLocalDate()));
        when(record.get(EWB_FINISH_DATE.getAlias(), Date.class)).thenReturn(Date.valueOf(NOW.toLocalDate()));
        when(record.get(EWB_TELEMECH_DECISION_OUT_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_TELEMECH_DECISION_IN_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_MEDIC_DECISION_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_TELEMECH_OUT_MILEAGE.getAlias(), Integer.class)).thenReturn(100);
        when(record.get(EWB_TELEMECH_IN_MILEAGE.getAlias(), Integer.class)).thenReturn(150);

        when(record.get(AUTHOR_FULL_NAME.getAlias(), String.class)).thenReturn("Иванов Иван Иванович");
        when(record.get(AUTHOR_PERSONNEL_NUMBER.getAlias(), String.class)).thenReturn("111223345");

        when(record.get(ATTORNEY_NUMBER.getAlias(), UUID.class)).thenReturn(
            UUID.fromString("2f4e3920-7a76-4b0f-8aa0-8bd545ed8989"));
        when(record.get(ATTORNEY_ISSUE_DATE.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(ATTORNEY_CREATION_SYSTEM.getAlias(), String.class)).thenReturn("KORUS");

        when(record.get(DRIVER_FULL_NAME.getAlias(), String.class)).thenReturn("Дядя Богдан");
        when(record.get(DRIVER_PERSONNEL_NUMBER.getAlias(), String.class)).thenReturn("555443321");
        when(record.get(DRIVER_ORGANIZATION_NAME.getAlias(), String.class)).thenReturn("ООО Пупок");
        when(record.get(DRIVER_DEPARTMENT_NAME.getAlias(), String.class)).thenReturn("Важные челики");

        when(record.get(DRIVING_LICENSE_SERIES.getAlias(), String.class)).thenReturn("1234");
        when(record.get(DRIVING_LICENSE_NUMBER.getAlias(), String.class)).thenReturn("12345");
        when(record.get(DRIVING_LICENSE_ISSUE_DATE.getAlias(), Date.class)).thenReturn(Date.valueOf(NOW.toLocalDate()));
        when(record.get(DRIVING_LICENSE_EXPIRY_DATE.getAlias(), Date.class)).thenReturn(
            Date.valueOf(NOW.toLocalDate()));

        when(record.get(TRANSPORT_STATE_NUMBER.getAlias(), String.class)).thenReturn("А777АА777");
        when(record.get(TRANSPORT_BRAND.getAlias(), String.class)).thenReturn("Mazda");
        when(record.get(TRANSPORT_MODEL.getAlias(), String.class)).thenReturn("6");
        when(record.get(TRANSPORT_TYPE_TITLE.getAlias(), String.class)).thenReturn("Служебный");
        when(record.get(TRANSPORT_SUBTYPE_TITLE.getAlias(), String.class)).thenReturn("СТС");

        when(record.get(MEDIC_ORGANIZATION_NAME.getAlias(), String.class)).thenReturn("ООО Кадык");
        when(record.get(MEDIC_FULL_NAME.getAlias(), String.class)).thenReturn("Михалыч Михаил");
        when(record.get(MEDIC_PERSONNEL_NUMBER.getAlias(), String.class)).thenReturn("111223344");

        when(record.get(MEDIC_REQUEST_HUMAN_READABLE_ID.getAlias(), String.class)).thenReturn("TL-0001-0000001");

        when(record.get(TELEMECH_OUT_ORGANIZATION_NAME.getAlias(), String.class)).thenReturn("ООО Кчау");
        when(record.get(TELEMECH_OUT_DEPARTMENT_NAME.getAlias(), String.class)).thenReturn("Метер-тягачи");
        when(record.get(TELEMECH_OUT_FULL_NAME.getAlias(), String.class)).thenReturn("Мак Вин");
        when(record.get(TELEMECH_OUT_PERSONNEL_NUMBER.getAlias(), String.class)).thenReturn("555443322");

        when(record.get(REQUEST_HUMAN_READABLE_ID.getAlias(), String.class)).thenReturn("TM-0001-0000001");

        when(record.get(TELEMECH_IN_FULL_NAME.getAlias(), String.class)).thenReturn("Док Старый");
        when(record.get(TELEMECH_IN_PERSONNEL_NUMBER.getAlias(), String.class)).thenReturn("555443323");

        var actual = EwbRegistryMapper.recordToEwbRegistryResponse(source);
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEwbRegistryInfo(actual.get(0).ewb());
        assertAuthorRegistryInfo(actual.get(0).author());
        assertAttorneyRegistryInfo(actual.get(0).attorney());
        assertDriverRegistryInfo(actual.get(0).driver());
        assertDrivingLicenseRegistryInfo(actual.get(0).drivingLicense());
        assertTransportRegistryInfo(actual.get(0).transport());
        assertMedicRegistryInfo(actual.get(0).medic());
        assertMedicRequestRegistryInfo(actual.get(0).medicRequest());
        assertTelemechOutRegistryInfo(actual.get(0).telemechOut());
        assertRequestRegistryInfo(actual.get(0).request());
        assertTelemechInRegistryInfo(actual.get(0).telemechIn());
    }

    @Test
    void shouldMapJooqRecordToEwbRegistryExcelAllOrganizationsDto() {
        var source = List.of(record);
        var fieldSet = Set.of(EWB_HUMAN_READABLE_ID, EWB_EWB_UUID, EWB_STATUS, EWB_CREATION_TIME, EWB_START_DATE,
            EWB_FINISH_DATE,
            EWB_TELEMECH_DECISION_OUT_TIME, EWB_TELEMECH_DECISION_IN_TIME, EWB_MEDIC_DECISION_TIME,
            EWB_TELEMECH_OUT_MILEAGE,
            EWB_TELEMECH_IN_MILEAGE, EWB_MEDIC_SUCCESS, EWB_TELEMECH_SUCCESS);

        when(record.field(anyString())).thenReturn(field);
        when(record.get(anyString())).thenReturn(new Object());
        when(record.get(EWB_HUMAN_READABLE_ID.getAlias(), String.class)).thenReturn("PL-0001-0000001");
        when(record.get(EWB_EWB_UUID.getAlias(), String.class)).thenReturn("49e3d6e4-c650-48ff-b827-143fda6a4d06");
        when(record.get(EWB_STATUS.getAlias(), String.class)).thenReturn(EwbStatus.ON_THE_LINE.name());
        when(record.get(EWB_CREATION_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_START_DATE.getAlias(), Date.class)).thenReturn(Date.valueOf(NOW.toLocalDate()));
        when(record.get(EWB_FINISH_DATE.getAlias(), Date.class)).thenReturn(Date.valueOf(NOW.toLocalDate()));
        when(record.get(EWB_TELEMECH_DECISION_OUT_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_TELEMECH_DECISION_IN_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_MEDIC_DECISION_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_TELEMECH_OUT_MILEAGE.getAlias(), Integer.class)).thenReturn(100);
        when(record.get(EWB_TELEMECH_IN_MILEAGE.getAlias(), Integer.class)).thenReturn(150);
        when(record.get("medicRequestStatus")).thenReturn(TelemedicineStatus.DONE.name());
        when(record.get("medicRequestStatus", String.class)).thenReturn(TelemedicineStatus.DONE.name());
        when(record.get("requestStatus")).thenReturn(RequestStatus.ON_THE_LINE.name());
        when(record.get("requestStatus", String.class)).thenReturn(RequestStatus.ON_THE_LINE.name());

        var actual = EwbRegistryMapper.recordToEwbRegistryExcelAllOrganizationsDto(source, fieldSet);
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(13, actual.get(0).getRegistry().size());
        assertEwbRegistryInfoAllOrganizationsExcel(actual.get(0));
    }

    @Test
    void shouldMapJooqRecordToEwbRegistryExcelSelfOrganizationDto() {
        var source = List.of(record);
        var fieldSet = Set.of(EWB_HUMAN_READABLE_ID, EWB_EWB_UUID, EWB_STATUS, EWB_CREATION_TIME, EWB_START_DATE,
            EWB_FINISH_DATE,
            EWB_TELEMECH_DECISION_OUT_TIME, EWB_TELEMECH_DECISION_IN_TIME, EWB_MEDIC_DECISION_TIME,
            EWB_TELEMECH_OUT_MILEAGE,
            EWB_TELEMECH_IN_MILEAGE, EWB_MEDIC_SUCCESS, EWB_TELEMECH_SUCCESS);

        when(record.field(anyString())).thenReturn(field);
        when(record.get(anyString())).thenReturn(new Object());
        when(record.get(EWB_HUMAN_READABLE_ID.getAlias(), String.class)).thenReturn("PL-0001-0000001");
        when(record.get(EWB_EWB_UUID.getAlias(), String.class)).thenReturn("49e3d6e4-c650-48ff-b827-143fda6a4d06");
        when(record.get(EWB_STATUS.getAlias(), String.class)).thenReturn(EwbStatus.ON_THE_LINE.name());
        when(record.get(EWB_CREATION_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_START_DATE.getAlias(), Date.class)).thenReturn(Date.valueOf(NOW.toLocalDate()));
        when(record.get(EWB_FINISH_DATE.getAlias(), Date.class)).thenReturn(Date.valueOf(NOW.toLocalDate()));
        when(record.get(EWB_TELEMECH_DECISION_OUT_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_TELEMECH_DECISION_IN_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_MEDIC_DECISION_TIME.getAlias(), Timestamp.class)).thenReturn(Timestamp.valueOf(NOW));
        when(record.get(EWB_TELEMECH_OUT_MILEAGE.getAlias(), Integer.class)).thenReturn(100);
        when(record.get(EWB_TELEMECH_IN_MILEAGE.getAlias(), Integer.class)).thenReturn(150);
        when(record.get("medicRequestStatus")).thenReturn(TelemedicineStatus.DONE.name());
        when(record.get("medicRequestStatus", String.class)).thenReturn(TelemedicineStatus.DONE.name());
        when(record.get("requestStatus")).thenReturn(RequestStatus.ON_THE_LINE.name());
        when(record.get("requestStatus", String.class)).thenReturn(RequestStatus.ON_THE_LINE.name());

        var actual = EwbRegistryMapper.recordToEwbRegistryExcelSelfOrganizationDto(source, fieldSet);
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(13, actual.get(0).getRegistry().size());
        assertEwbRegistryInfoSelfOrganizationExcel(actual.get(0));
    }

    private void assertEwbRegistryInfo(EwbRegistryInfo actual) {
        assertEquals("PL-0001-0000001", actual.humanReadableId());
        assertEquals("49e3d6e4-c650-48ff-b827-143fda6a4d06", actual.ewbUuid());
        assertEquals(EwbStatus.ON_THE_LINE, actual.status());
        assertEquals(NOW, actual.creationTime());
        assertEquals(NOW.toLocalDate(), actual.startDate());
        assertEquals(NOW.toLocalDate(), actual.finishDate());
        assertEquals(NOW, actual.telemechDecisionOutTime());
        assertEquals(NOW, actual.telemechDecisionInTime());
        assertEquals(NOW, actual.medicDecisionTime());
        assertEquals(100, actual.telemechOutMileage());
        assertEquals(150, actual.telemechInMileage());
        assertTrue(actual.medicSuccess());
        assertTrue(actual.telemechSuccess());
    }

    private void assertEwbRegistryInfoAllOrganizationsExcel(EwbRegistryExcelAllOrganizationsDto actual) {
        var map = actual.getRegistry();
        assertEquals("PL-0001-0000001", map.get(EWB_HUMAN_READABLE_ID.getExcelColumnName()));
        assertEquals("49e3d6e4-c650-48ff-b827-143fda6a4d06", map.get(EWB_EWB_UUID.getExcelColumnName()));
        assertEquals(EwbStatus.ON_THE_LINE.getRusName(), map.get(EWB_STATUS.getExcelColumnName()));
        assertEquals(
            OffsetDateTime.of(NOW, ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Europe/Moscow")).format(TIME_FORMATTER),
            map.get(EWB_CREATION_TIME.getExcelColumnName()));
        assertEquals(NOW.format(DATE_FORMATTER), map.get(EWB_START_DATE.getExcelColumnName()));
        assertEquals(NOW.format(DATE_FORMATTER), map.get(EWB_FINISH_DATE.getExcelColumnName()));
        assertEquals(
            OffsetDateTime.of(NOW, ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Europe/Moscow")).format(TIME_FORMATTER),
            map.get(EWB_TELEMECH_DECISION_OUT_TIME.getExcelColumnName()));
        assertEquals(
            OffsetDateTime.of(NOW, ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Europe/Moscow")).format(TIME_FORMATTER),
            map.get(EWB_TELEMECH_DECISION_IN_TIME.getExcelColumnName()));
        assertEquals(String.valueOf(100), map.get(EWB_TELEMECH_OUT_MILEAGE.getExcelColumnName()));
        assertEquals(String.valueOf(150), map.get(EWB_TELEMECH_IN_MILEAGE.getExcelColumnName()));
        assertEquals("Прошел предсменный медицинский осмотр, к исполнению трудовых обязанностей допущен",
            map.get(EWB_MEDIC_SUCCESS.getExcelColumnName()));
        assertEquals("Выпуск на линию разрешен", map.get(EWB_TELEMECH_SUCCESS.getExcelColumnName()));
    }

    private void assertEwbRegistryInfoSelfOrganizationExcel(EwbRegistryExcelSelfOrganizationDto actual) {
        var map = actual.getRegistry();
        assertEquals("PL-0001-0000001", map.get(EWB_HUMAN_READABLE_ID.getExcelColumnName()));
        assertEquals("49e3d6e4-c650-48ff-b827-143fda6a4d06", map.get(EWB_EWB_UUID.getExcelColumnName()));
        assertEquals(EwbStatus.ON_THE_LINE.getRusName(), map.get(EWB_STATUS.getExcelColumnName()));
        assertEquals(
            OffsetDateTime.of(NOW, ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Europe/Moscow")).format(TIME_FORMATTER),
            map.get(EWB_CREATION_TIME.getExcelColumnName()));
        assertEquals(NOW.format(DATE_FORMATTER), map.get(EWB_START_DATE.getExcelColumnName()));
        assertEquals(NOW.format(DATE_FORMATTER), map.get(EWB_FINISH_DATE.getExcelColumnName()));
        assertEquals(
            OffsetDateTime.of(NOW, ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Europe/Moscow")).format(TIME_FORMATTER),
            map.get(EWB_TELEMECH_DECISION_OUT_TIME.getExcelColumnName()));
        assertEquals(
            OffsetDateTime.of(NOW, ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Europe/Moscow")).format(TIME_FORMATTER),
            map.get(EWB_TELEMECH_DECISION_IN_TIME.getExcelColumnName()));
        assertEquals(String.valueOf(100), map.get(EWB_TELEMECH_OUT_MILEAGE.getExcelColumnName()));
        assertEquals(String.valueOf(150), map.get(EWB_TELEMECH_IN_MILEAGE.getExcelColumnName()));
        assertEquals("Прошел предсменный медицинский осмотр, к исполнению трудовых обязанностей допущен",
            map.get(EWB_MEDIC_SUCCESS.getExcelColumnName()));
        assertEquals("Выпуск на линию разрешен", map.get(EWB_TELEMECH_SUCCESS.getExcelColumnName()));
    }

    private void assertAuthorRegistryInfo(AuthorRegistryInfo actual) {
        assertEquals("Иванов Иван Иванович", actual.fullName());
        assertEquals("111223345", actual.personnelNumber());
    }

    private void assertAttorneyRegistryInfo(AttorneyRegistryInfo actual) {
        assertEquals(UUID.fromString("2f4e3920-7a76-4b0f-8aa0-8bd545ed8989"), actual.number());
        assertEquals(NOW, actual.issueDate());
        assertEquals("KORUS", actual.creationSystem());
    }

    private void assertDriverRegistryInfo(DriverRegistryInfo actual) {
        assertEquals("Дядя Богдан", actual.fullName());
        assertEquals("555443321", actual.personnelNumber());
        assertEquals("ООО Пупок", actual.organizationName());
        assertEquals("Важные челики", actual.departmentName());
    }

    private void assertDrivingLicenseRegistryInfo(DrivingLicenseRegistryInfo actual) {
        assertEquals("1234", actual.series());
        assertEquals("12345", actual.number());
        assertEquals(NOW.toLocalDate(), actual.issueDate());
        assertEquals(NOW.toLocalDate(), actual.expiryDate());
    }

    private void assertTransportRegistryInfo(TransportRegistryInfo actual) {
        assertEquals("А777АА777", actual.stateNumber());
        assertEquals("Mazda", actual.brand());
        assertEquals("6", actual.model());
        assertEquals("Служебный", actual.type());
        assertEquals("СТС", actual.subtype());
    }

    private void assertMedicRegistryInfo(MedicRegistryInfo actual) {
        assertEquals("ООО Кадык", actual.organizationName());
        assertEquals("Михалыч Михаил", actual.fullName());
        assertEquals("111223344", actual.personnelNumber());
    }

    private void assertMedicRequestRegistryInfo(MedicRequestRegistryInfo actual) {
        assertEquals("TL-0001-0000001", actual.humanReadableId());
    }

    private void assertTelemechOutRegistryInfo(TelemechOutRegistryInfo actual) {
        assertEquals("ООО Кчау", actual.organizationName());
        assertEquals("Метер-тягачи", actual.departmentName());
        assertEquals("Мак Вин", actual.fullName());
        assertEquals("555443322", actual.personnelNumber());
    }

    private void assertRequestRegistryInfo(RequestRegistryInfo actual) {
        assertEquals("TM-0001-0000001", actual.humanReadableId());
    }

    private void assertTelemechInRegistryInfo(TelemechInRegistryInfo actual) {
        assertEquals("Док Старый", actual.fullName());
        assertEquals("555443323", actual.personnelNumber());
    }

}
