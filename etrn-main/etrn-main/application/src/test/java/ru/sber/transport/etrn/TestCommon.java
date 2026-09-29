package ru.sber.transport.etrn;

import ru.sber.transport.messaging.kafka.test.KafkaTest;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Базовый класс для всех тестов проекта ЭТрН.
 * Содержит общие константы (UUID, идентификаторы, роли) и
 * подготовленные объекты для использования в тестах.
 */
public class TestCommon extends KafkaTest {

    public static final UUID ORGANIZATION_ID_1 = UUID.fromString("a0408b2c-2334-11eb-9b73-305a3a7d9fe6");
    public static final UUID ORGANIZATION_ID_2 = UUID.fromString("c2055154-8d02-4e1a-ac57-eadbaca44026");
    public static final Long ORGANIZATION_DIGIT_ID_1 = 100001L;
    public static final Long ORGANIZATION_DIGIT_ID_2 = 100002L;
    public static final String ORGANIZATION_NAME_1 = "ООО «ТестЛогистик»";
    public static final String ORGANIZATION_NAME_2 = "ООО «ТранспортСервис»";
    public static final String ORGANIZATION_ADDRESS_1 = "г. Москва, ул. Тестовая, д. 1";
    public static final String ORGANIZATION_ADDRESS_2 = "г. Санкт-Петербург, ул. Промышленная, д. 10";

    public static final UUID DEPARTMENT_ID_1 = UUID.fromString("b1519c3d-3445-22fc-0c84-416b4b8e0f17");
    public static final UUID DEPARTMENT_ID_2 = UUID.fromString("c2620d4e-4556-33fd-1d95-527c5c9f1e28");
    public static final String DEPARTMENT_NAME_1 = "Отдел перевозок";
    public static final String DEPARTMENT_HRID_1 = "DP-0001-001";
    public static final String DEPARTMENT_HRID_2 = "DP-0002-001";

    public static final UUID EMPLOYEE_ID_1 = UUID.fromString("f10bcc5b-51db-4e1c-a747-2a229604f974");
    public static final UUID EMPLOYEE_ID_2 = UUID.fromString("a26a3382-674d-4497-9411-815303250ee1");
    public static final UUID EMPLOYEE_USER_ID_1 = UUID.fromString("f10bcc5b-51db-4e1c-a747-2a229604f974");
    public static final UUID EMPLOYEE_USER_ID_2 = UUID.fromString("a26a3382-674d-4497-9411-815303250ee1");
    public static final String EMPLOYEE_HRID_1 = "US-0001-1";
    public static final String EMPLOYEE_HRID_2 = "US-0002-2";
    public static final String EMPLOYEE_PERSONNEL_NO_1 = "PN00001";
    public static final String EMPLOYEE_PERSONNEL_NO_2 = "PN00002";
    public static final String EMPLOYEE_FIRST_NAME_1 = "Иван";
    public static final String EMPLOYEE_LAST_NAME_1 = "Петров";
    public static final String EMPLOYEE_FIRST_NAME_2 = "Мария";
    public static final String EMPLOYEE_LAST_NAME_2 = "Иванова";

    public static final String USER_ID_1_STR = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public static final String USER_ID_2_STR = "a26a3382-674d-4497-9411-815303250ee1";
    public static final String USER_ID_3_STR = "a0408b2c-2334-11eb-9b73-305a3a7d9fe6";
    public static final String USER_ID_4_STR = "c2055154-8d02-4e1a-ac57-eadbaca44026";
    public static final String USER_ID_5_STR = "f62d200a-1720-4c76-bee9-047c26766c1f";

    public static final String ROLE_ADMIN_DATA_MASTER = "ROLE_ADMIN_DATA_MASTER";
    public static final String ROLE_DISPATCHER_SUPPORT_SERVICE = "ROLE_DISPATCHER_SUPPORT_SERVICE";
    public static final String ROLE_GUEST = "ROLE_GUEST";

    public static final UUID ETRN_ID_1 = UUID.fromString("cf8f42df-998a-48b4-b349-1a33f0c1072b");
    public static final UUID ETRN_ID_2 = UUID.fromString("df9f53ee-009b-59c5-c450-2b44f1d2083c");
    public static final String ETRN_HRID_1 = "ETRN-0001-00000001";
    public static final String ETRN_HRID_2 = "ETRN-0002-00000002";
    public static final String ETRN_HRID_3 = "ETRN-0003-00000003";

    public static final String STATUS_IDENTIFIED = "IDENTIFIED";
    public static final String STATUS_WAIT_KORUS_DATA = "WAIT_KORUS_DATA";
    public static final String STATUS_READY_FOR_BANK_ACTION = "READY_FOR_BANK_ACTION";
    public static final String STATUS_WAIT_CONDITIONS = "WAIT_CONDITIONS";
    public static final String STATUS_WAIT_KORUS_CONFIRMATION = "WAIT_KORUS_CONFIRMATION";
    public static final String STATUS_PROCESS_COMPLETED = "PROCESS_COMPLETED";

    public static final String TIME_ZONE = "Europe/Moscow";
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1, 15, 10, 0, 0);
    public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2025, 1, 15, 12, 0, 0);
}
