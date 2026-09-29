package ru.sber.transport.telemechanic.mapper;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.Test;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.using;
import static ru.sber.transport.telemechanic.enumerate.MedicRequestField.*;

class MedicRequestRegistryMapperTest {
    
    @Test
    void recordToRegistryResponse() {
        var fieldSet = Set.of(EWB_ID, EWB_UUID, EWB_HUMAN_READABLE_ID, EWB_MEDIC_DECISION_TIME, MEDIC_REQUEST_HUMAN_READABLE_ID,
                              MEDIC_REQUEST_SYSTOLIC_PRESSURE, MEDIC_REQUEST_DIASTOLIC_PRESSURE, MEDIC_REQUEST_PULSE, MEDIC_REQUEST_TEMPERATURE,
                              MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT, MEDIC_REQUEST_STATUS, MEDIC_FULL_NAME, MEDIC_PERSONNEL_NUMBER,
                              MEDIC_ORGANIZATION_NAME, MEDIC_DEPARTMENT_NAME, MEDIC_LICENSE_SERIES, MEDIC_LICENSE_NUMBER, MEDIC_LICENSE_ISSUE_DATE,
                              MEDIC_LICENSE_EXPIRY_DATE, DRIVER_FULL_NAME, DRIVER_PERSONNEL_NUMBER, DRIVER_ORGANIZATION_NAME, DRIVER_DEPARTMENT_NAME,
                              DRIVER_LICENSE_SERIES, DRIVER_LICENSE_NUMBER, DRIVER_LICENSE_ISSUE_DATE, DRIVER_LICENSE_EXPIRY_DATE);
        var record = List.of(createRecord());
        var actual = MedicRequestRegistryMapper.recordToRegistryResponse(record, fieldSet);
        assertThat(actual).hasSize(1);
        assertThat(actual)
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                                                  .builder()
                                                  .build())
                .isEqualTo(List.of(createExpectedMap()));
    }
    
    private Record createRecord() {
        var dslContext = using(SQLDialect.POSTGRES);
        return fillRecord(
                dslContext.newRecord(
                        Arrays.stream(values())
                                .map(f -> field(f.getAlias()))
                                .toList()
                                    )
                         );
    }
    
    private Record fillRecord(Record record) {
        record.setValue(field(EWB_ID.getAlias()), "2d497a93-f1f0-47f7-a0e4-fcaf1b9f7801");
        record.setValue(field(EWB_UUID.getAlias()), "218447c3-4b34-441e-ae98-9f73b7c27f65");
        record.setValue(field(EWB_HUMAN_READABLE_ID.getAlias()), "PL-0000-00000000");
        record.setValue(field(EWB_MEDIC_DECISION_TIME.getAlias()), Timestamp.valueOf("2000-01-01 00:00:00"));
        
        record.setValue(field(MEDIC_REQUEST_HUMAN_READABLE_ID.getAlias()), "TL-0000-000000");
        record.setValue(field(MEDIC_REQUEST_SYSTOLIC_PRESSURE.getAlias()), 120);
        record.setValue(field(MEDIC_REQUEST_DIASTOLIC_PRESSURE.getAlias()), 80);
        record.setValue(field(MEDIC_REQUEST_PULSE.getAlias()), 60);
        record.setValue(field(MEDIC_REQUEST_TEMPERATURE.getAlias()), 36.6);
        record.setValue(field(MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT.getAlias()), 0.0);
        record.setValue(field(MEDIC_REQUEST_STATUS.getAlias()), "DONE");
        
        record.setValue(field(MEDIC_FULL_NAME.getAlias()), "Петров Петр Петрович");
        record.setValue(field(MEDIC_PERSONNEL_NUMBER.getAlias()), "00000000");
        record.setValue(field(MEDIC_ORGANIZATION_NAME.getAlias()), "ООО Компания");
        record.setValue(field(MEDIC_DEPARTMENT_NAME.getAlias()), "Бригада");
        
        record.setValue(field(MEDIC_LICENSE_SERIES.getAlias()), "11111");
        record.setValue(field(MEDIC_LICENSE_NUMBER.getAlias()), "123456789");
        record.setValue(field(MEDIC_LICENSE_ISSUE_DATE.getAlias()), Date.valueOf("2000-01-01"));
        record.setValue(field(MEDIC_LICENSE_EXPIRY_DATE.getAlias()), Date.valueOf("2000-01-01"));
        
        record.setValue(field(DRIVER_FULL_NAME.getAlias()), "Иванов Иван Иванович");
        record.setValue(field(DRIVER_PERSONNEL_NUMBER.getAlias()), "00000001");
        record.setValue(field(DRIVER_ORGANIZATION_NAME.getAlias()), "ООО Компания");
        record.setValue(field(DRIVER_DEPARTMENT_NAME.getAlias()), "Бригада");
        
        record.setValue(field(DRIVER_LICENSE_SERIES.getAlias()), "22222");
        record.setValue(field(DRIVER_LICENSE_NUMBER.getAlias()), "123456789");
        record.setValue(field(DRIVER_LICENSE_ISSUE_DATE.getAlias()), Date.valueOf("2000-01-01"));
        record.setValue(field(DRIVER_LICENSE_EXPIRY_DATE.getAlias()), Date.valueOf("2000-01-01"));
        
        return record;
    }
    
    private Map<String, Object> createExpectedMap() {
        var map = new HashMap<String, Object>();
        map.put(EWB_ID.getAlias(), "2d497a93-f1f0-47f7-a0e4-fcaf1b9f7801");
        map.put(EWB_UUID.getAlias(), "218447c3-4b34-441e-ae98-9f73b7c27f65");
        map.put(EWB_HUMAN_READABLE_ID.getAlias(), "PL-0000-00000000");
        map.put(EWB_MEDIC_DECISION_TIME.getAlias(), 946684800000L);
        
        map.put(MEDIC_REQUEST_HUMAN_READABLE_ID.getAlias(), "TL-0000-000000");
        map.put(MEDIC_REQUEST_SYSTOLIC_PRESSURE.getAlias(), 120);
        map.put(MEDIC_REQUEST_DIASTOLIC_PRESSURE.getAlias(), 80);
        map.put(MEDIC_REQUEST_PULSE.getAlias(), 60);
        map.put(MEDIC_REQUEST_TEMPERATURE.getAlias(), 36.6);
        map.put(MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT.getAlias(), 0.0);
        map.put(MEDIC_REQUEST_STATUS.getAlias(), TelemedicineStatus.DONE.getDescription());
        
        map.put(MEDIC_FULL_NAME.getAlias(), "Петров Петр Петрович");
        map.put(MEDIC_PERSONNEL_NUMBER.getAlias(), "00000000");
        map.put(MEDIC_ORGANIZATION_NAME.getAlias(), "ООО Компания");
        map.put(MEDIC_DEPARTMENT_NAME.getAlias(), "Бригада");
        
        map.put(MEDIC_LICENSE_SERIES.getAlias(), "11111");
        map.put(MEDIC_LICENSE_NUMBER.getAlias(), "123456789");
        map.put(MEDIC_LICENSE_ISSUE_DATE.getAlias(), LocalDate.of(2000, 1, 1));
        map.put(MEDIC_LICENSE_EXPIRY_DATE.getAlias(), LocalDate.of(2000, 1, 1));
        
        map.put(DRIVER_FULL_NAME.getAlias(), "Иванов Иван Иванович");
        map.put(DRIVER_PERSONNEL_NUMBER.getAlias(), "00000001");
        map.put(DRIVER_ORGANIZATION_NAME.getAlias(), "ООО Компания");
        map.put(DRIVER_DEPARTMENT_NAME.getAlias(), "Бригада");
        
        map.put(DRIVER_LICENSE_SERIES.getAlias(), "22222");
        map.put(DRIVER_LICENSE_NUMBER.getAlias(), 123456789);
        map.put(DRIVER_LICENSE_ISSUE_DATE.getAlias(), LocalDate.of(2000, 1, 1));
        map.put(DRIVER_LICENSE_EXPIRY_DATE.getAlias(), LocalDate.of(2000, 1, 1));
        
        return map;
    }
    
}
