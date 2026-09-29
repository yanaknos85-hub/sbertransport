package ru.sber.transport.telemechanic.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MedicRequestField {
    EWB_ID("e1_0.id", "ewbId", null),
    EWB_UUID("e1_0.ewb_uuid", "ewbUuid", "Номер ЭПЛ ГИС ЭПД"),
    EWB_HUMAN_READABLE_ID("e1_0.human_readable_id", "ewbHumanReadableId", "Номер ЭПЛ"),
    EWB_MEDIC_DECISION_TIME("e1_0.medic_decision_time", "ewbMedicDecisionTime", "Дата и время проведения предсменного, предрейсового медицинского осмотра"),
    
    MEDIC_REQUEST_HUMAN_READABLE_ID("m1_0.human_readable_id", "medicRequestHumanReadableId", "Номер медицинского осмотра"),
    MEDIC_REQUEST_SYSTOLIC_PRESSURE("m1_0.syst_pressure", "medicRequestSystolicPressure", "Давление артериальное Систолическое"),
    MEDIC_REQUEST_DIASTOLIC_PRESSURE("m1_0.dyast_pressure", "medicRequestDiastolicPressure", "Давление артериальное Диастолическое"),
    MEDIC_REQUEST_PULSE("m1_0.pulse", "medicRequestPulse", "Пульс"),
    MEDIC_REQUEST_TEMPERATURE("m1_0.temperature", "medicRequestTemperature", "Температура"),
    MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT("m1_0.blood_alcohol", "medicRequestBreathAlcoholTestResult", "Показание алкоголя"),
    MEDIC_REQUEST_STATUS("m1_0.status", "medicRequestStatus", "Отметка о результате проведения предсменного, предрейсового медицинского осмотра"),
    
    MEDIC_FULL_NAME("""
                    case
                        when e1_0.medic_id is not null then
                            (a1_0.last_name || ' ' || a1_0.first_name || COALESCE(' ' || a1_0.patronymic, ''))
                        when e1_0.medic_contractor_id is not null then
                            m2_0.full_name
                    end
                    """, "medicFullName", "ФИО Медицинского работника"),
    MEDIC_PERSONNEL_NUMBER("""
                    case
                        when e1_0.medic_id is not null then
                            a1_0.personnel_number
                        when e1_0.medic_contractor_id is not null then
                            m2_0.personnel_number
                    end
                    """, "medicPersonnelNumber", "Табельный  номер мед работника"),
    MEDIC_ORGANIZATION_NAME("""
                    case
                        when e1_0.medic_id is not null then
                            o1_0.official_name
                        when e1_0.medic_contractor_id is not null then
                            m2_0.organization
                    end
                    """, "medicOrganizationName", "Организация мед работника"),
    MEDIC_DEPARTMENT_NAME("""
                    case
                        when e1_0.medic_id is not null then
                            d1_0.department_name
                        when e1_0.medic_contractor_id is not null then
                            m2_0.department
                    end
                    """, "medicDepartmentName", "Подразделение мед работника"),
    
    MEDIC_LICENSE_SERIES("b1_0.series", "medicLicenseSeries", "Серия лицензии"),
    MEDIC_LICENSE_NUMBER("b1_0.number", "medicLicenseNumber", "Номер лицензии"),
    MEDIC_LICENSE_ISSUE_DATE("b1_0.issue_date", "medicLicenseIssueDate", "Дата выдачи лицензии"),
    MEDIC_LICENSE_EXPIRY_DATE("b1_0.expiry_date", "medicLicenseExpiryDate", "Дата окончания срока действия лицензии"),
    
    DRIVER_FULL_NAME("(a2_0.last_name || ' ' || a2_0.first_name || COALESCE(' ' || a2_0.patronymic, ''))", "driverFullName", "ФИО водителя"),
    DRIVER_PERSONNEL_NUMBER("a2_0.personnel_number", "driverPersonnelNumber", "Табельный номер водителя"),
    DRIVER_ORGANIZATION_NAME("o2_0.official_name", "driverOrganizationName", "Организация водителя"),
    DRIVER_DEPARTMENT_NAME("d2_0.department_name", "driverDepartmentName", "Подразделение водителя"),
    DRIVER_TIN("dr1_0.tin", "driverTin", "ИНН водителя"),
    
    DRIVER_LICENSE_SERIES("c1_0.series", "drivingLicenseSeries", "Серия водительских прав"),
    DRIVER_LICENSE_NUMBER("c1_0.number", "drivingLicenseNumber", "Номер водительских прав"),
    DRIVER_LICENSE_ISSUE_DATE("c1_0.issue_date", "drivingLicenseIssueDate", "Дата выдачи водительских прав"),
    DRIVER_LICENSE_EXPIRY_DATE("c1_0.expiry_date", "drivingLicenseExpiryDate", "Дата истечения водительских прав");
    
    private final String sqlViewFieldName;
    private final String alias;
    private final String excelColumnName;
}
