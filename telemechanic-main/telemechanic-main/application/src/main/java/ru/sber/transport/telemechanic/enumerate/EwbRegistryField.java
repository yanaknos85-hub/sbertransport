package ru.sber.transport.telemechanic.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EwbRegistryField {
    EWB_HUMAN_READABLE_ID("ewb.human_readable_id", "humanReadableId", "Номер путевого листа"),
    EWB_EWB_UUID("ewb.ewb_uuid", "ewbUuid", "Номер ЭПЛ ГИС"),
    EWB_STATUS("ewb.status", "status", "Статус Путевого листа"),
    EWB_CREATION_TIME("ewb.creation_time", "creationTime", "Дата и время создания ЭПЛ"),
    EWB_START_DATE("ewb.start_date", "startDate", "Дата начала действия путевого листа"),
    EWB_FINISH_DATE("ewb.finish_date", "finishDate", "Дата окончания действия путевого листа"),
    EWB_TELEMECH_DECISION_OUT_TIME("ewb.telemech_decision_out", "telemechDecisionOutTime", "Дата выезда"),
    EWB_TELEMECH_DECISION_IN_TIME("ewb.telemech_decision_in", "telemechDecisionInTime", "Дата возвращения"),
    EWB_MEDIC_DECISION_TIME("ewb.medic_decision_time", "medicDecisionTime", "Дата и время принятия решения медика"),
    EWB_TELEMECH_OUT_MILEAGE("ewb.odometer_out", "telemechOutMileage", "Одометр при выезде"),
    EWB_TELEMECH_IN_MILEAGE("ewb.odometer_in", "telemechInMileage", "Одометр при заезде"),
    EWB_MEDIC_SUCCESS("ewb.medic_decision_time", "medicDecisionTime2", "Результат медицинского контроля"),
    EWB_TELEMECH_SUCCESS("ewb.telemech_decision_out", "telemechDecisionOutTime2", "Результат технического контроля"),
    
    AUTHOR_FULL_NAME("(auth.last_name || ' ' || auth.first_name || COALESCE(' ' || auth.patronymic, ''))", "authorFullName", "ФИО Создателя ЭПЛ"),
    AUTHOR_PERSONNEL_NUMBER("auth.personnel_number", "authorPersonnelNumber", "Табельный номер создателя ЭПЛ"),
    
    ATTORNEY_NUMBER("att.number", "attorneyNumber", "Номер доверенности"),
    ATTORNEY_ISSUE_DATE("att.issue_date", "attorneyIssueDate", "Дата доверенности"),
    ATTORNEY_CREATION_SYSTEM("att.creation_system", "attorneyCreationSystem", "Система хранения доверенности"),
    
    DRIVER_FULL_NAME("(emp.last_name || ' ' || emp.first_name || COALESCE(' ' || emp.patronymic, ''))", "driverFullName", "ФИО Водителя"),
    DRIVER_PERSONNEL_NUMBER("emp.personnel_number", "driverPersonnelNumber", "Табельный номер водителя"),
    DRIVER_ORGANIZATION_NAME("org.official_name", "organizationName", "Организация владелец ТС"),
    DRIVER_DEPARTMENT_NAME("dep.department_name", "departmentName", "Подразделение владелец ТС"),
    
    DRIVING_LICENSE_SERIES("dl.series", "drivingLicenseSeries", "Серия прав"),
    DRIVING_LICENSE_NUMBER("dl.number", "drivingLicenseNumber", "Номер прав"),
    DRIVING_LICENSE_ISSUE_DATE("dl.issue_date", "drivingLicenseIssueDate", "Дата выдачи прав"),
    DRIVING_LICENSE_EXPIRY_DATE("dl.expiry_date", "drivingLicenseExpiryDate", "Дата окончания прав"),
    
    TRANSPORT_STATE_NUMBER("tr.state_number", "transportStateNumber", "Государственный номер"),
    TRANSPORT_BRAND("tr.brand", "transportBrand", "Марка"),
    TRANSPORT_MODEL("tr.model", "transportModel", "Модель"),
    TRANSPORT_TYPE_TITLE("tr.type", "transportType", "Вид ТС"),
    TRANSPORT_SUBTYPE_TITLE("tr.subtype", "transportSubtype", "Подвид ТС"),
    
    MEDIC_ORGANIZATION_NAME("""
                            case
                            when ewb.medic_id is not null then
                                med_org.official_name
                            when ewb.medic_contractor_id is not null then
                                med_con.organization
                            end
                            """,
            "medicOrganizationName", "Организация медицинского осмотра"),
    MEDIC_FULL_NAME("""
                    case
                    when ewb.medic_id is not null then
                        (med.last_name || ' ' || med.first_name || COALESCE(' ' || med.patronymic, ''))
                    when ewb.medic_contractor_id is not null then
                        med_con.full_name
                    end
                    """,
                    "medicFullName", "ФИО ответственного за медицинский осмотр"),
    MEDIC_PERSONNEL_NUMBER("""
                           case
                           when ewb.medic_id is not null then
                                med.personnel_number
                           when ewb.medic_contractor_id is not null then
                                med_con.personnel_number
                           end
                           """,
                           "medicPersonnelNumber", "Табельный номер медицинского работника"),
    
    MEDIC_REQUEST_HUMAN_READABLE_ID("mr.human_readable_id", "medicRequestHumanReadableId", "Номер заявки на медицинский осмотр"),
    
    TELEMECH_OUT_ORGANIZATION_NAME("tm_out_org.official_name", "telemechOutOrganizationName", "Организация технического осмотра"),
    TELEMECH_OUT_DEPARTMENT_NAME("tm_out_dep.department_name", "telemechOutDepartmentName", "Подразделение технического осмотра"),
    TELEMECH_OUT_FULL_NAME("(tm_out.last_name || ' ' || tm_out.first_name || COALESCE(' ' || tm_out.patronymic, ''))", "telemechOutFullName", "ФИО ответственного за техническое состояние"),
    TELEMECH_OUT_PERSONNEL_NUMBER("tm_out.personnel_number", "telemechOutPersonnelNumber", "Табельный номер механика"),
    
    REQUEST_HUMAN_READABLE_ID("r.human_readable_id", "requestHumanReadableId", "Номер заявки на технический осмотр"),
    
    TELEMECH_IN_FULL_NAME("(tm_in.last_name || ' ' || tm_in.first_name || COALESCE(' ' || tm_in.patronymic, ''))", "telemechInFullName", "ФИО ответственного за заезд ТС (механик)"),
    TELEMECH_IN_PERSONNEL_NUMBER("tm_in.personnel_number", "telemechInPersonnelNumber", "Табельный номер механика");
    
    
    private final String sqlViewFieldName;
    private final String alias;
    private final String excelColumnName;
}
