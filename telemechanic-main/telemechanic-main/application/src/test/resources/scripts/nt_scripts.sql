--Local
-- organizationId = 'a81daffb-811f-4e77-b249-51ff71993113';
-- rootDepartmentId = '7305d4fe-ffb2-47fb-942c-bd7c974ac705';
-- positionId = '0bcf60b0-e41f-474e-badc-3913dccb7e06';
--Dev-fleet
-- organizationId = '00c724c5-c116-4edf-a228-7a68cbe5b19b';
-- rootDepartmentId = '79c08000-2bca-4aef-a1e1-cdd442b0b00b';
-- positionId = '8b7bf8a6-04ee-4e6b-b30f-afd9ea79a1f5';
--Создание данных телемеханик
DO
$do$
    DECLARE
        vehicleId          telemechanic.transport.id%TYPE;
        requestId          telemechanic.request.id%TYPE;
        employeeId         telemechanic.employee.id%TYPE;
        departmentId       telemechanic.department.id%TYPE;
        parentDepartmentId telemechanic.department.parent_id%TYPE;
        rootDepartmentId   telemechanic.department.id%TYPE;
        organizationId     telemechanic.department.organization_id%TYPE;
        positionId         telemechanic.position.id%TYPE;
        checkId1           telemechanic."check".id%TYPE;
        checkId2           telemechanic."check".id%TYPE;
        checkId3           telemechanic."check".id%TYPE;
        checkId4           telemechanic."check".id%TYPE;
        checkId5           telemechanic."check".id%TYPE;
        checkId6           telemechanic."check".id%TYPE;
        checkId7           telemechanic."check".id%TYPE;
        checkId8           telemechanic."check".id%TYPE;
        checkId9           telemechanic."check".id%TYPE;
        checkId10          telemechanic."check".id%TYPE;
        checkId11          telemechanic."check".id%TYPE;
        checkId12          telemechanic."check".id%TYPE;
        checkId13          telemechanic."check".id%TYPE;
        checkId14          telemechanic."check".id%TYPE;
        checkId15          telemechanic."check".id%TYPE;
        checkId16          telemechanic."check".id%TYPE;
        checkId17          telemechanic."check".id%TYPE;
        checkId18          telemechanic."check".id%TYPE;
        checkId19          telemechanic."check".id%TYPE;
        checkId20          telemechanic."check".id%TYPE;
        requestSeq         telemechanic.company_sq.sq%TYPE;
        departmentSeq      corporate.company_sq.sq%TYPE;
        employeeSeq        corporate.company_sq.sq%TYPE;
        digitId            corporate.organization.digit_id%TYPE;
        nowTime            timestamp;
        nowTimeMinus1Day   timestamp;
        nowTImePlus1Day    timestamp;
    BEGIN
        organizationId = 'a81daffb-811f-4e77-b249-51ff71993113';
        rootDepartmentId = '7305d4fe-ffb2-47fb-942c-bd7c974ac705';
        positionId = '0bcf60b0-e41f-474e-badc-3913dccb7e06';
        nowTime = CURRENT_TIMESTAMP;
        nowTimeMinus1Day = nowTime - INTERVAL '1 DAY';
        nowTImePlus1Day = nowTime + INTERVAL '1 DAY';

        select digit_id into digitId from corporate.organization where id = organizationId;
        select sq into requestSeq from telemechanic.company_sq where orgdigitid = digitId and prefix = 'TM';
        select sq into departmentSeq from corporate.company_sq where orgdigitid = digitId and prefix = 'DT';
        select sq into employeeSeq from corporate.company_sq where orgdigitid = digitId and prefix = 'US';
        alter table telemechanic.check_photo drop constraint check_photo_check_id_fk;
        alter table telemechanic."check" drop constraint check_request_cid_fk;
        alter table telemechanic.request_history drop constraint request_history_employee_id_fk;
        alter table telemechanic.request_history drop constraint request_history_request_id_fk;
        alter table telemechanic.request drop constraint request_employee_id_fk;
        alter table telemechanic.request drop constraint request_vehicle_id_fk;
        alter table telemechanic.request drop constraint request_employee_id_fk2;
        FOR i IN 1..20 LOOP
                INSERT INTO telemechanic.department (id, active, department_name, human_readable_id, organization_id, parent_id)
                VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), true,
                        (select 'Потомок_удаляемый_xxx' || i),
                        (select 'DT-' || TO_CHAR(1, 'fm0000') || '-' || TO_CHAR(departmentSeq, 'fm00000000')), organizationId, rootDepartmentId)
                returning id into parentDepartmentId;
                departmentSeq = departmentSeq + 1;
--                 update corporate.company_sq set sq = sq + 1, dt_modify = nowTime where prefix = 'DT' and orgdigitid = digitId;
                FOR i IN 1..40 LOOP
                        departmentId = parentDepartmentId;
--                         INSERT INTO telemechanic.department (id, active, department_name, human_readable_id,
--                                                              organization_id,
--                                                              parent_id)
--                         VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), true,
--                                 (select 'Потомок_удаляемый_xxx' || i),
--                                 (select 'DT-' || TO_CHAR(1, 'fm0000') || '-' || TO_CHAR(departmentSeq, 'fm00000000'))
--                                    , organizationId, departmentId)
--                         returning id into departmentId;
--                         departmentSeq = departmentSeq + 1;
                        --Создание заявки в статусе DONE
                        begin
                            INSERT INTO telemechanic.transport (id, brand, state_number, model)
                            VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                    'Lada',
                                    (select (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (SELECT lpad(floor(random() * 1000)::text, 3, '0')) ||
                                            (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (SELECT floor(random() * 1000)::int)),
                                    '2101')
                            returning id into vehicleId;
                        exception
                            when unique_violation then
                                INSERT INTO telemechanic.transport (id, brand, state_number, model)
                                VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                        'Lada',
                                        (select (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (SELECT lpad(floor(random() * 1000)::text, 3, '0')) ||
                                                (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (SELECT floor(random() * 1000)::int)),
                                        '2101')
                                returning id into vehicleId;
                        end;
                        INSERT INTO telemechanic.employee (id, active, first_name, human_readable_id, last_name, mobile_phone,
                                                           patronymic, personnel_number, user_id, department_id,
                                                           position_id, organization_id)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), true, 'Мусор',
                                (select 'US-' || TO_CHAR(1, 'fm0000') || '-' || TO_CHAR(
                                        employeeSeq, 'fm00000000')),
                                'Удаляемый', null, 'Мусорович',
                                (SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                (SELECT md5(random()::text || clock_timestamp()::text)::uuid), departmentId,
                                positionId, organizationId)
                        returning id into employeeId;
                        employeeSeq = employeeSeq + 1;
                        insert into telemechanic.request (id, human_readable_id, author_id, creation_time, vehicle_id, status, comment, inspector_id, inspection_time)
                        values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                (select 'TM-' || TO_CHAR(1, 'fm0000') || '-'|| TO_CHAR(requestSeq, 'fm00000000')),
                                employeeId,
                                nowTime,
                                vehicleId,
                                'DONE',
                                null,
                                null,
                                null)
                        returning id into requestId;
                        requestSeq = requestSeq + 1;
                        INSERT INTO telemechanic.request_history (id, change_time, request_status, comment, initiator_id, request_id)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), nowTime, 'IN_PROGRESS', 'Статус заявки изменен пользователем: Удаляемыый Мусор Мусорович', employeeId, requestId),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), nowTImePlus1Day, 'DONE', 'Проверки завершены успешно', employeeId, requestId);
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'VEHICLE_NUMBER', 2, requestId, null) returning id into checkId1;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'OIL_LEVEL', 0, requestId, null) returning id into checkId2;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'POWER_STEERING_LIQUID_LEVEL', 0, requestId, null) returning id into checkId3;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'COOLANT_LEVEL', 0, requestId, null) returning id into checkId4;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_LF', 0, requestId, null) returning id into checkId5;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_LR', 0, requestId, null) returning id into checkId6;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_RF', 0, requestId, null) returning id into checkId7;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_RR', 1, requestId, null) returning id into checkId8;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS', 0, requestId, null) returning id into checkId9;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SIDE_MIRRORS_L', 0, requestId, null) returning id into checkId10;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SIDE_MIRRORS_R', 2, requestId, null) returning id into checkId11;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SIDE_MIRRORS', 0, requestId, null) returning id into checkId12;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'WIND_SCREEN', 0, requestId, null) returning id into checkId13;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'WINDSHIELD_WIPERS_AND_LIQUID', 0, requestId, null) returning id into checkId14;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS_LF', 0, requestId, null) returning id into checkId15;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS_LR', 0, requestId, null) returning id into checkId16;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS_RF', 0, requestId, null) returning id into checkId17;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS_RR', 2, requestId, null) returning id into checkId18;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS', 0, requestId, null) returning id into checkId19;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'TIRE_TREAD', 0, requestId, null) returning id into checkId20;
                        INSERT INTO telemechanic.check_photo (id, check_id, creation_time, file_status)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId1, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId1, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId2, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId3, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId4, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId5, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId6, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId7, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId8, nowTime, 'NOT_UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId8, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId10, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId11, nowTime, 'NOT_UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId11, nowTime, 'NOT_UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId11, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId13, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId14, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId15, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId16, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId17, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId18, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId18, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId18, nowTime, 'UPLOADED');
                    end loop;
                FOR i IN 1..170 LOOP
                        departmentId = parentDepartmentId;
                        --                         INSERT INTO telemechanic.department (id, active, department_name, human_readable_id,
--                                                              organization_id,
--                                                              parent_id)
--                         VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), true,
--                                 (select 'Потомок_удаляемый_xxx' || i),
--                                 (select 'DT-' || TO_CHAR(1, 'fm0000') || '-' || TO_CHAR(departmentSeq, 'fm00000000'))
--                                    , organizationId, departmentId)
--                         returning id into departmentId;
--                         departmentSeq = departmentSeq + 1;
                        --Создание заявки в статусе IN_PROGRESS
                        begin
                            INSERT INTO telemechanic.transport (id, brand, state_number, model)
                            VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                    'Lada',
                                    (select (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (SELECT lpad(floor(random() * 1000)::text, 3, '0')) ||
                                            (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (SELECT floor(random() * 1000)::int)),
                                    '2101')
                            returning id into vehicleId;
                        exception
                            when unique_violation then
                                INSERT INTO telemechanic.transport (id, brand, state_number, model)
                                VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                        'Lada',
                                        (select (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (SELECT lpad(floor(random() * 1000)::text, 3, '0')) ||
                                                (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (SELECT floor(random() * 1000)::int)),
                                        '2101')
                                returning id into vehicleId;
                        end;
                        INSERT INTO telemechanic.employee (id, active, first_name, human_readable_id, last_name, mobile_phone,
                                                           patronymic, personnel_number, user_id, department_id,
                                                           position_id, organization_id)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), true, 'Мусор',
                                (select 'US-' || TO_CHAR(1, 'fm0000') || '-' || TO_CHAR(
                                        employeeSeq, 'fm00000000')),
                                'Удаляемый', null, 'Мусорович',
                                (SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                (SELECT md5(random()::text || clock_timestamp()::text)::uuid), departmentId,
                                positionId, organizationId)
                        returning id into employeeId;
                        employeeSeq = employeeSeq + 1;
                        insert into telemechanic.request (id, human_readable_id, author_id, creation_time, vehicle_id, status, comment, inspector_id, inspection_time)
                        values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                (select 'TM-' || TO_CHAR(1, 'fm0000') || '-'|| TO_CHAR(requestSeq, 'fm00000000')),
                                employeeId,
                                nowTime,
                                vehicleId,
                                'IN_PROGRESS',
                                null,
                                null,
                                null)
                        returning id into requestId;
                        requestSeq = requestSeq + 1;
                        INSERT INTO telemechanic.request_history (id, change_time, request_status, comment, initiator_id, request_id)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), nowTime, 'IN_PROGRESS', 'Статус заявки изменен пользователем: Удаляемыый Мусор Мусорович', employeeId, requestId);
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'VEHICLE_NUMBER', 1, requestId, null) returning id into checkId1;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'OIL_LEVEL', 0, requestId, null) returning id into checkId2;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'POWER_STEERING_LIQUID_LEVEL', 0, requestId, null) returning id into checkId3;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'COOLANT_LEVEL', 0, requestId, null) returning id into checkId4;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_LF', 0, requestId, null) returning id into checkId5;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_LR', 0, requestId, null) returning id into checkId6;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_RF', 0, requestId, null) returning id into checkId7;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_RR', 0, requestId, null) returning id into checkId8;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS', 0, requestId, null) returning id into checkId9;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SIDE_MIRRORS_L', 0, requestId, null) returning id into checkId10;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'IN_PROGRESS', 'SIDE_MIRRORS_R', 1, requestId, null) returning id into checkId11;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'IN_PROGRESS', 'SIDE_MIRRORS', 0, requestId, null) returning id into checkId12;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'IN_PROGRESS', 'WIND_SCREEN', 0, requestId, null) returning id into checkId13;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'IN_PROGRESS', 'WINDSHIELD_WIPERS_AND_LIQUID', 0, requestId, null) returning id into checkId14;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'IN_PROGRESS', 'HEADLAMPS_LF', 0, requestId, null) returning id into checkId15;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'IN_PROGRESS', 'HEADLAMPS_LR', 0, requestId, null) returning id into checkId16;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'IN_PROGRESS', 'HEADLAMPS_RF', 0, requestId, null) returning id into checkId17;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'IN_PROGRESS', 'HEADLAMPS_RR', 0, requestId, null) returning id into checkId18;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'IN_PROGRESS', 'HEADLAMPS', 0, requestId, null) returning id into checkId19;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'IN_PROGRESS', 'TIRE_TREAD', 0, requestId, null) returning id into checkId20;
                        INSERT INTO telemechanic.check_photo (id, check_id, creation_time, file_status)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId1, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId1, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId2, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId3, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId4, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId5, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId6, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId7, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId8, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId10, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId11, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId11, nowTime, 'UPLOADED');
                    end loop;
                FOR i IN 1..350 LOOP
                        departmentId = parentDepartmentId;
                        --                         INSERT INTO telemechanic.department (id, active, department_name, human_readable_id,
--                                                              organization_id,
--                                                              parent_id)
--                         VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), true,
--                                 (select 'Потомок_удаляемый_xxx' || i),
--                                 (select 'DT-' || TO_CHAR(1, 'fm0000') || '-' || TO_CHAR(departmentSeq, 'fm00000000'))
--                                    , organizationId, departmentId)
--                         returning id into departmentId;
--                         departmentSeq = departmentSeq + 1;
                        --Создание заявки в статусе WARNING
                        begin
                            INSERT INTO telemechanic.transport (id, brand, state_number, model)
                            VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                    'Lada',
                                    (select (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (SELECT lpad(floor(random() * 1000)::text, 3, '0')) ||
                                            (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (SELECT floor(random() * 1000)::int)),
                                    '2101')
                            returning id into vehicleId;
                        exception
                            when unique_violation then
                                INSERT INTO telemechanic.transport (id, brand, state_number, model)
                                VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                        'Lada',
                                        (select (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (SELECT lpad(floor(random() * 1000)::text, 3, '0')) ||
                                                (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (SELECT floor(random() * 1000)::int)),
                                        '2101')
                                returning id into vehicleId;
                        end;
                        INSERT INTO telemechanic.employee (id, active, first_name, human_readable_id, last_name, mobile_phone,
                                                           patronymic, personnel_number, user_id, department_id,
                                                           position_id, organization_id)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), true, 'Мусор',
                                (select 'US-' || TO_CHAR(1, 'fm0000') || '-' || TO_CHAR(
                                        employeeSeq, 'fm00000000')),
                                'Удаляемый', null, 'Мусорович',
                                (SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                (SELECT md5(random()::text || clock_timestamp()::text)::uuid), departmentId,
                                positionId, organizationId)
                        returning id into employeeId;
                        employeeSeq = employeeSeq + 1;
                        insert into telemechanic.request (id, human_readable_id, author_id, creation_time, vehicle_id, status, comment, inspector_id, inspection_time)
                        values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                (select 'TM-' || TO_CHAR(1, 'fm0000') || '-'|| TO_CHAR(requestSeq, 'fm00000000')),
                                employeeId,
                                nowTime,
                                vehicleId,
                                'WARNING',
                                null,
                                null,
                                null)
                        returning id into requestId;
                        requestSeq = requestSeq + 1;
                        INSERT INTO telemechanic.request_history (id, change_time, request_status, comment, initiator_id, request_id)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), nowTime, 'IN_PROGRESS', 'Статус заявки изменен пользователем: Удаляемыый Мусор Мусорович', employeeId, requestId),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), nowTImePlus1Day, 'WARNING', 'Проверки завершены с замечаниями', employeeId, requestId);
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'VEHICLE_NUMBER', 1, requestId, null) returning id into checkId1;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'OIL_LEVEL', 0, requestId, null) returning id into checkId2;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'POWER_STEERING_LIQUID_LEVEL', 0, requestId, null) returning id into checkId3;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'COOLANT_LEVEL', 0, requestId, null) returning id into checkId4;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_LF', 0, requestId, null) returning id into checkId5;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_LR', 0, requestId, null) returning id into checkId6;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_RF', 0, requestId, null) returning id into checkId7;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_RR', 0, requestId, null) returning id into checkId8;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS', 0, requestId, null) returning id into checkId9;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SIDE_MIRRORS_L', 0, requestId, null) returning id into checkId10;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SIDE_MIRRORS_R', 1, requestId, null) returning id into checkId11;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SIDE_MIRRORS', 0, requestId, null) returning id into checkId12;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'WIND_SCREEN', 0, requestId, null) returning id into checkId13;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'WINDSHIELD_WIPERS_AND_LIQUID', 0, requestId, null) returning id into checkId14;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS_LF', 0, requestId, null) returning id into checkId15;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS_LR', 0, requestId, null) returning id into checkId16;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS_RF', 0, requestId, null) returning id into checkId17;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DECLINE', 'HEADLAMPS_RR', 2, requestId, null) returning id into checkId18;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DECLINE', 'HEADLAMPS', 0, requestId, null) returning id into checkId19;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DECLINE', 'TIRE_TREAD', 2, requestId, null) returning id into checkId20;
                        INSERT INTO telemechanic.check_photo (id, check_id, creation_time, file_status)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId1, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId1, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId2, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId3, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId4, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId5, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId6, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId7, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId8, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId10, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId11, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId11, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId13, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId14, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId15, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId16, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId17, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId18, nowTime, 'NOT_UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId18, nowTime, 'NOT_UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId18, nowTime, 'NOT_UPLOADED');
                    end loop;
                FOR i IN 1..250 LOOP
                        departmentId = parentDepartmentId;
--                         INSERT INTO telemechanic.department (id, active, department_name, human_readable_id,
--                                                              organization_id,
--                                                              parent_id)
--                         VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), true,
--                                 (select 'Потомок_удаляемый_xxx' || i),
--                                 (select 'DT-' || TO_CHAR(1, 'fm0000') || '-' || TO_CHAR(departmentSeq, 'fm00000000'))
--                                    , organizationId, departmentId)
--                         returning id into departmentId;
--                         departmentSeq = departmentSeq + 1;
                        --Создание заявки в статусе ON_THE_LINE
                        begin
                            INSERT INTO telemechanic.transport (id, brand, state_number, model)
                            VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                    'Lada',
                                    (select (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (SELECT lpad(floor(random() * 1000)::text, 3, '0')) ||
                                            (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                            (SELECT floor(random() * 1000)::int)),
                                    '2101')
                            returning id into vehicleId;
                        exception
                            when unique_violation then
                                INSERT INTO telemechanic.transport (id, brand, state_number, model)
                                VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                        'Lada',
                                        (select (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (SELECT lpad(floor(random() * 1000)::text, 3, '0')) ||
                                                (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (ARRAY ['A','B','Е','К','М','Н','О','Р','С','Т','У','Х'])[(random() * 10 + 2)::int] ||
                                                (SELECT floor(random() * 1000)::int)),
                                        '2101')
                                returning id into vehicleId;
                        end;
                        INSERT INTO telemechanic.employee (id, active, first_name, human_readable_id, last_name, mobile_phone,
                                                           patronymic, personnel_number, user_id, department_id,
                                                           position_id, organization_id)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), true, 'Мусор',
                                (select 'US-' || TO_CHAR(1, 'fm0000') || '-' || TO_CHAR(employeeSeq, 'fm00000000')),
                                'Удаляемый', null, 'Мусорович',
                                (SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                (SELECT md5(random()::text || clock_timestamp()::text)::uuid), departmentId,
                                positionId, organizationId)
                        returning id into employeeId;
                        employeeSeq = employeeSeq + 1;
                        insert into telemechanic.request (id, human_readable_id, author_id, creation_time, vehicle_id, status, comment, inspector_id, inspection_time)
                        values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid),
                                (select 'TM-' || TO_CHAR(1, 'fm0000') || '-'|| TO_CHAR(requestSeq, 'fm00000000')),
                                employeeId,
                                nowTime,
                                vehicleId,
                                'ON_THE_LINE',
                                '1234',
                                employeeId,
                                nowTime)
                        returning id into requestId;
                        requestSeq = requestSeq + 1;
                        INSERT INTO telemechanic.request_history (id, change_time, request_status, comment, initiator_id, request_id)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), nowTime, 'IN_PROGRESS', 'Статус заявки изменен пользователем: Удаляемыый Мусор Мусорович', employeeId, requestId),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), nowTImePlus1Day, 'DONE', 'Проверки завершены успешно', employeeId, requestId),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), nowTImePlus1Day, 'ON_THE_LINE',  'На линии', employeeId, requestId);
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'VEHICLE_NUMBER', 1, requestId, null) returning id into checkId1;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'OIL_LEVEL', 0, requestId, null) returning id into checkId2;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'POWER_STEERING_LIQUID_LEVEL', 0, requestId, null) returning id into checkId3;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'COOLANT_LEVEL', 0, requestId, null) returning id into checkId4;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_LF', 0, requestId, null) returning id into checkId5;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_LR', 0, requestId, null) returning id into checkId6;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_RF', 0, requestId, null) returning id into checkId7;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS_RR', 1, requestId, null) returning id into checkId8;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SPLASH_GUARDS', 0, requestId, null) returning id into checkId9;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SIDE_MIRRORS_L', 0, requestId, null) returning id into checkId10;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SIDE_MIRRORS_R', 2, requestId, null) returning id into checkId11;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'SIDE_MIRRORS', 0, requestId, null) returning id into checkId12;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'WIND_SCREEN', 0, requestId, null) returning id into checkId13;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'WINDSHIELD_WIPERS_AND_LIQUID', 0, requestId, null) returning id into checkId14;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS_LF', 0, requestId, null) returning id into checkId15;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS_LR', 0, requestId, null) returning id into checkId16;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS_RF', 0, requestId, null) returning id into checkId17;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS_RR', 2, requestId, null) returning id into checkId18;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'HEADLAMPS', 0, requestId, null) returning id into checkId19;
                        INSERT INTO telemechanic."check" (id, status, check_type, attempt, request_id, comment) values ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), 'DONE', 'TIRE_TREAD', 0, requestId, null) returning id into checkId20;
                        INSERT INTO telemechanic.check_photo (id, check_id, creation_time, file_status)
                        VALUES ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId1, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId1, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId2, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId3, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId4, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId5, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId6, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId7, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId8, nowTime, 'NOT_UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId8, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId10, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId11, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId11, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId11, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId13, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId14, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId15, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId16, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId17, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId18, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId18, nowTime, 'UPLOADED'),
                               ((SELECT md5(random()::text || clock_timestamp()::text)::uuid), checkId18, nowTime, 'UPLOADED');
                    end loop;
            END LOOP;
        update corporate.company_sq set sq = departmentSeq + 1, dt_modify = nowTime where prefix = 'DT' and orgdigitid = digitId;
        update corporate.company_sq set sq = employeeSeq + 1, dt_modify = nowTime where prefix = 'US' and orgdigitid = digitId;
        update telemechanic.company_sq set sq = requestSeq + 1, dt_modify = nowTime where prefix = 'TM' and orgdigitid = digitId;
        alter table telemechanic.request add constraint request_employee_id_fk foreign key (author_id) references telemechanic.employee;
        alter table telemechanic.request add constraint request_employee_id_fk2 foreign key (inspector_id) references telemechanic.employee;
        alter table telemechanic.request add constraint request_vehicle_id_fk foreign key (vehicle_id) references telemechanic.transport;
        alter table telemechanic.request_history add constraint request_history_employee_id_fk foreign key (initiator_id) references telemechanic.employee;
        alter table telemechanic.request_history add constraint request_history_request_id_fk foreign key (request_id) references telemechanic.request;
        alter table telemechanic."check" add constraint check_request_cid_fk foreign key (request_id) references telemechanic.request;
        alter table telemechanic.check_photo add constraint check_photo_check_id_fk foreign key (check_id) references telemechanic."check";
    END
$do$;
--Удаление данных телемеханик
DO
$do$
    BEGIN
        alter table telemechanic.check_photo drop constraint check_photo_check_id_fk;
        alter table telemechanic."check" drop constraint check_request_cid_fk;
        alter table telemechanic.request_history drop constraint request_history_employee_id_fk;
        alter table telemechanic.request_history drop constraint request_history_request_id_fk;
        alter table telemechanic.request drop constraint request_employee_id_fk;
        alter table telemechanic.request drop constraint request_vehicle_id_fk;
        alter table telemechanic.request drop constraint request_employee_id_fk2;

        delete from telemechanic.check_photo where check_id in (select id from telemechanic."check" where request_id in (select id from telemechanic.request where author_id in (select id from telemechanic.employee where employee.last_name like 'Удаляемый%')));
        delete from telemechanic."check" where request_id in (select id from telemechanic.request where author_id in (select id from telemechanic.employee where employee.last_name like 'Удаляемый%'));
        delete from telemechanic.request_history where request_id in (select id from telemechanic.request where author_id in (select id from telemechanic.employee where employee.last_name like 'Удаляемый%'));
        delete from telemechanic.request where author_id in (select id from telemechanic.employee where employee.last_name like 'Удаляемый%');
        delete from telemechanic.transport where id not in (select vehicle_id from telemechanic.request);
        delete from telemechanic.employee where last_name like 'Удаляемый%';
        delete from telemechanic.department where department_name like 'Потомок_удаляемый_%';

        alter table telemechanic.request add constraint request_employee_id_fk foreign key (author_id) references telemechanic.employee;
        alter table telemechanic.request add constraint request_employee_id_fk2 foreign key (inspector_id) references telemechanic.employee;
        alter table telemechanic.request add constraint request_vehicle_id_fk foreign key (vehicle_id) references telemechanic.transport;
        alter table telemechanic.request_history add constraint request_history_employee_id_fk foreign key (initiator_id) references telemechanic.employee;
        alter table telemechanic.request_history add constraint request_history_request_id_fk foreign key (request_id) references telemechanic.request;
        alter table telemechanic."check" add constraint check_request_cid_fk foreign key (request_id) references telemechanic.request;
        alter table telemechanic.check_photo add constraint check_photo_check_id_fk foreign key (check_id) references telemechanic."check";
    END
$do$;
--Подсчет количества заказов телемеханик
select count(1) from telemechanic.request;