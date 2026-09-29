insert into vehicle.category (id, category, title) values ('be8abe82-1cbd-4f0e-9b31-2b9db90231df', 'В', 'Легковые автомобили, небольшие грузовики (до 3,5 тонн)');
insert into vehicle.category (id, category, title) values ('63cf7084-5052-49aa-aac9-e03c398839c6', 'С', 'Грузовые автомобили (от 3,5 тонн)');

insert into vehicle.organization (id, digit_id, active, official_name) values ('ca9874fd-3b54-4150-bd24-935ede298453', 1001, true, 'Тест орга');

insert into vehicle.department (id, active, department_name, human_readable_id, organization_id, parent_id)
values ('4c781ce7-20cd-4733-ba1d-ffc75fc98498', true, 'Тест подразделение 1', 'DEP-1-01', 'ca9874fd-3b54-4150-bd24-935ede298453', null);
insert into vehicle.department (id, active, department_name, human_readable_id, organization_id, parent_id)
values ('74cbd748-e787-43b5-91b1-3431104c7f73', true, 'Тест подразделение 2', 'DEP-1-02', 'ca9874fd-3b54-4150-bd24-935ede298453', null);

insert into vehicle.position (id, active, organization_id, position_name)
values ('48fa9e87-ed0b-46b8-a868-ec17d242ac81', true, 'ca9874fd-3b54-4150-bd24-935ede298453', 'Должность важная');

insert into vehicle.employee (id, active, human_readable_id, first_name, last_name,
                              patronymic, mobile_phone, personnel_number, user_id,
                              department_id, position_id, organization_id)
values ('a6e866e5-fea9-4984-b1a7-f49bec8ea5fa', true, 'EMP-1-01', 'Тест', 'Тестов', 'Тестович', '123', '1110001',
        'a6e866e5-fea9-4984-b1a7-f49bec8ea5fa', '4c781ce7-20cd-4733-ba1d-ffc75fc98498', '48fa9e87-ed0b-46b8-a868-ec17d242ac81',
        'ca9874fd-3b54-4150-bd24-935ede298453'),
       ('31adca6a-5ab7-4cb1-8397-271896b68193', true, 'EMP-1-02', 'Тест', 'Брестов', 'Тестович', '124', '1110002',
        '31adca6a-5ab7-4cb1-8397-271896b68193', '74cbd748-e787-43b5-91b1-3431104c7f73', '48fa9e87-ed0b-46b8-a868-ec17d242ac81',
        'ca9874fd-3b54-4150-bd24-935ede298453');

insert into vehicle.driving_license (id, driver_id, series, number, issue_date, expiry_date, previous_id)
values ('8f843ac0-59e8-4278-baa2-c9efb26373de', 'a6e866e5-fea9-4984-b1a7-f49bec8ea5fa', 'aa01', 10001, now(), now(), null);
insert into vehicle.driving_license (id, driver_id, series, number, issue_date, expiry_date, previous_id)
values ('917e4056-38a6-4dff-9329-3b4ef04a1414', '31adca6a-5ab7-4cb1-8397-271896b68193', 'aa01', 10002, now(), now(), null);

insert into vehicle.driving_license_category (driving_license_id, category_id) values ('8f843ac0-59e8-4278-baa2-c9efb26373de',
                                                                                       'be8abe82-1cbd-4f0e-9b31-2b9db90231df');
insert into vehicle.driving_license_category (driving_license_id, category_id) values ('8f843ac0-59e8-4278-baa2-c9efb26373de',
                                                                                       '63cf7084-5052-49aa-aac9-e03c398839c6');
insert into vehicle.driving_license_category (driving_license_id, category_id) values ('917e4056-38a6-4dff-9329-3b4ef04a1414',
                                                                                       'be8abe82-1cbd-4f0e-9b31-2b9db90231df');
insert into vehicle.driving_license_category (driving_license_id, category_id) values ('917e4056-38a6-4dff-9329-3b4ef04a1414',
                                                                                       '63cf7084-5052-49aa-aac9-e03c398839c6');