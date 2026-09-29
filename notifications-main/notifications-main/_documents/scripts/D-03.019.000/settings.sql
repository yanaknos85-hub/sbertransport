-- Согласование заявки
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('715f8ff7-3bd0-4afc-ad4b-0de6136a77d4', 'REQUEST_CARGO', 'Согласование заявки', 'notice_801',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'APPROVE', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('9c919939-aa5a-4cd9-a072-5f1308ca0e07', '715f8ff7-3bd0-4afc-ad4b-0de6136a77d4', 'PUSH',
        'На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}',
        true);

INSERT INTO notifications_settings.channel
VALUES ('b34aaf34-5d99-4efb-8cd8-f8b01bfdc76d', '715f8ff7-3bd0-4afc-ad4b-0de6136a77d4', 'EMAIL',
        'На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}',
        false);

INSERT INTO notifications_settings.channel
VALUES ('ad9676ef-c698-48ac-9dcb-f12c48152982', '715f8ff7-3bd0-4afc-ad4b-0de6136a77d4', 'SMS',
        'На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}',
        false);

INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
VALUES ('ad9676ef-c698-48ac-9dcb-f12c48152982', 0, 'AT_EVENT', '715f8ff7-3bd0-4afc-ad4b-0de6136a77d4', null, null);



-- Статус согласования
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('200c14b7-6edc-4135-b7d9-62e6ef90e32d', 'REQUEST_CARGO', 'Статус согласования', 'notice_802',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'APPROVE_STATUS', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('1ff202a4-fb33-4751-afe7-f384572f2a13', '200c14b7-6edc-4135-b7d9-62e6ef90e32d', 'PUSH',
        'Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}',
        true);

INSERT INTO notifications_settings.channel
VALUES ('d232a418-f8bf-4587-919f-94da65fb3b02', '200c14b7-6edc-4135-b7d9-62e6ef90e32d', 'EMAIL',
        'Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}',
        false);

INSERT INTO notifications_settings.channel
VALUES ('5fd72228-632b-4a0a-b145-6b42e20961d8', '200c14b7-6edc-4135-b7d9-62e6ef90e32d', 'SMS',
        'Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}',
        false);

INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
VALUES ('5fd72228-632b-4a0a-b145-6b42e20961d8', 0, 'AT_EVENT', '200c14b7-6edc-4135-b7d9-62e6ef90e32d', null, null);


-- Формирование маршрута
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('8bbd1ad2-124c-43f7-9402-885317c3cde6', 'REQUEST_CARGO', 'Формирование маршрута', 'notice_803',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_PLANNING', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('6b57a235-93b0-47fe-99c2-9c84a77f45d6', '8bbd1ad2-124c-43f7-9402-885317c3cde6', 'PUSH',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('143a8f69-2680-45dd-9106-db10f1b5ac30', '8bbd1ad2-124c-43f7-9402-885317c3cde6', 'EMAIL',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('d018748a-ed85-4804-b918-9fd746143777', '8bbd1ad2-124c-43f7-9402-885317c3cde6', 'SMS',
        '',
        false);

-- Маршрут направлен контрагенту
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('401adc42-9694-49fa-ba97-984037122727', 'REQUEST_CARGO', 'Маршрут направлен контрагенту', 'notice_804',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_PLANNING', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('4f7676b8-69aa-4542-8b06-f2004bd1b8ad', '401adc42-9694-49fa-ba97-984037122727', 'PUSH',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('1db06855-795d-4be3-b685-b2be70aa264b', '401adc42-9694-49fa-ba97-984037122727', 'EMAIL',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('aeab8d0a-4ee0-4f79-a5bf-db03e0eab771', '401adc42-9694-49fa-ba97-984037122727', 'SMS',
        '',
        false);

-- Назначение водителя/курьера на маршрут
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('463566a4-fd0b-4961-ba12-40a47ada32db', 'REQUEST_CARGO', 'Назначение водителя/курьера на маршрут', 'notice_805',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_PLANNING', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('6cca121d-0500-4fea-8772-a3c8e014ed94', '463566a4-fd0b-4961-ba12-40a47ada32db', 'PUSH',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('6e0b5b63-6e45-4c94-92e3-119f6e3f3310', '463566a4-fd0b-4961-ba12-40a47ada32db', 'EMAIL',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('15b909c9-b572-4cb1-b00e-d2f8b048eb1f', '463566a4-fd0b-4961-ba12-40a47ada32db', 'SMS',
        '',
        false);

-- Назначение водителя на заявку
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('c089da5f-3012-4cb2-999e-4901b625dc2f', 'REQUEST_CARGO', 'Назначение водителя/курьера на заявку', 'notice_806',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_DRIVER_AWAITING_DATA', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('189e3efa-c158-4688-8946-9758cc25b3b4', 'c089da5f-3012-4cb2-999e-4901b625dc2f', 'PUSH',
        'На заявку {humanReadableId} назначен водитель {authorFio} и автомобиль {carInfo}',
        true);

INSERT INTO notifications_settings.channel
VALUES ('0eaf14e6-ede1-4870-a77c-2cefacbc6df6', 'c089da5f-3012-4cb2-999e-4901b625dc2f', 'EMAIL',
        'На заявку {humanReadableId} назначен водитель {authorFio} и автомобиль {carInfo}',
        false);

INSERT INTO notifications_settings.channel
VALUES ('71dd0916-e4fa-434e-8ede-b3fe2baa0d0b', 'c089da5f-3012-4cb2-999e-4901b625dc2f', 'SMS',
        '',
        false);

INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
VALUES ('71dd0916-e4fa-434e-8ede-b3fe2baa0d0b', 0, 'AT_EVENT', 'c089da5f-3012-4cb2-999e-4901b625dc2f', null, null);

-- Назначение курьера на заявку
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('c089da5f-3012-4cb2-999e-4901b625dccc', 'REQUEST_CARGO', 'Назначение водителя/курьера на заявку', 'notice_817',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_COURIER_AWAITING_DATA', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('189e3efa-c158-4688-8946-9758cc25b333', 'c089da5f-3012-4cb2-999e-4901b625dccc', 'PUSH',
        'На заявку {humanReadableId} назначен курьер',
        true);

INSERT INTO notifications_settings.channel
VALUES ('0eaf14e6-ede1-4870-a77c-2cefacbc6ddd', 'c089da5f-3012-4cb2-999e-4901b625dccc', 'EMAIL',
        'На заявку {humanReadableId} назначен курьер',
        false);

INSERT INTO notifications_settings.channel
VALUES ('71dd0916-e4fa-434e-8ede-b3fe2baa0ddd', 'c089da5f-3012-4cb2-999e-4901b625dccc', 'SMS',
        '',
        false);

INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
VALUES ('71dd0916-e4fa-434e-8ede-b3fe2baa0ddd', 0, 'AT_EVENT', 'c089da5f-3012-4cb2-999e-4901b625dccc', null, null);


-- Водитель/курьер выехал на маршрут
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('ea01fd99-16e5-4d3c-ae7c-4ba2aab4c1cc', 'REQUEST_CARGO', 'Водитель/курьер выехал на маршрут', 'notice_807',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_AWAITING_TRANSFER', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('a1b9c8b0-acab-49dd-ad24-8f1d72dc4afc', 'ea01fd99-16e5-4d3c-ae7c-4ba2aab4c1cc', 'PUSH',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('0de2147d-e91a-4544-a398-f3f7f5b66191', 'ea01fd99-16e5-4d3c-ae7c-4ba2aab4c1cc', 'EMAIL',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('251672c1-6d99-4c67-8481-ef818b5e6dbd', 'ea01fd99-16e5-4d3c-ae7c-4ba2aab4c1cc', 'SMS',
        '',
        false);

INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
VALUES ('251672c1-6d99-4c67-8481-ef818b5e6dbd', 0, 'AT_EVENT', 'ea01fd99-16e5-4d3c-ae7c-4ba2aab4c1cc', null, null);


-- Водитель/курьер на точке загрузки
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('19d3a139-6a6d-4c73-a3ab-4440301c467a', 'REQUEST_CARGO', 'Водитель/курьер на точке загрузки', 'notice_808',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_AWAITING_TRANSFER', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('1c3dbc12-a03a-4b23-98f9-4d7b0cb268ea', '19d3a139-6a6d-4c73-a3ab-4440301c467a', 'PUSH',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('8d64faff-9c7f-41fe-8183-08f992cb342d', '19d3a139-6a6d-4c73-a3ab-4440301c467a', 'EMAIL',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('a34494cb-129e-4f87-813d-cf9097a8b5a2', '19d3a139-6a6d-4c73-a3ab-4440301c467a', 'SMS',
        '',
        false);

-- Водитель/курьер на точке разгрузки
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('bf65a2be-9682-4cf0-a4ab-97b82718e72a', 'REQUEST_CARGO', 'Водитель/курьер на точке разгрузки', 'notice_809',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_TRANSFER_FINISHED', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('f27d61a0-d721-432f-bea4-58e2d5ac29ad', 'bf65a2be-9682-4cf0-a4ab-97b82718e72a', 'PUSH',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('49ff9cc3-6722-428c-9c32-8dcee6ef3edb', 'bf65a2be-9682-4cf0-a4ab-97b82718e72a', 'EMAIL',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('36b7ca73-4e50-4293-9fda-1b149b7c9b48', 'bf65a2be-9682-4cf0-a4ab-97b82718e72a', 'SMS',
        '',
        false);

-- Поездка завершена
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('2bfe7531-e2d8-488e-982f-d527f597d7f1', 'REQUEST_CARGO', 'Поездка завершена', 'notice_810',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_DELIVERY_CONFIRMATION_FINISHED', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('e1441ac3-7ca1-4119-adc4-6d49b75d2e7d', '2bfe7531-e2d8-488e-982f-d527f597d7f1', 'PUSH',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('6a0d0083-cf39-4379-a69d-f0a2ac96a81c', '2bfe7531-e2d8-488e-982f-d527f597d7f1', 'EMAIL',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('f3d92800-6fd7-4694-9b8e-e7ee434d4c39', '2bfe7531-e2d8-488e-982f-d527f597d7f1', 'SMS',
        '',
        false);

-- Заявка исполнена - оценка
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('2deab241-a08a-4d28-8689-e4ea418ca563', 'REQUEST_CARGO', 'Заявка исполнена - оценка', 'notice_811',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_SHIPMENT_FINISHED', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('b9f8718e-0e08-41f9-b2d9-9a63d5e58ef9', '2deab241-a08a-4d28-8689-e4ea418ca563', 'PUSH',
        'Заявка {humanReadableId} на грузоперевозку исполнена. Оцените качество предоставленных услуг',
        true);

INSERT INTO notifications_settings.channel
VALUES ('3c7d3822-8c77-45ff-95d5-987346cc9372', '2deab241-a08a-4d28-8689-e4ea418ca563', 'EMAIL',
        'Заявка {humanReadableId} на грузоперевозку исполнена. Оцените качество предоставленных услуг',
        false);

INSERT INTO notifications_settings.channel
VALUES ('98bc4e0f-d725-4672-8e50-ebc5bd1c210c', '2deab241-a08a-4d28-8689-e4ea418ca563', 'SMS',
        'Заявка {humanReadableId} на грузоперевозку исполнена. Оцените качество предоставленных услуг',
        false);


INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
VALUES ('98bc4e0f-d725-4672-8e50-ebc5bd1c210c', 0, 'AT_EVENT', '2deab241-a08a-4d28-8689-e4ea418ca563', null, null);

-- Поездка завершена
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('d8cdc0fe-9e00-4790-a1fb-5fddb84ed845', 'REQUEST_CARGO', 'Поездка завершена', 'notice_812',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'APPROVE', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('f83d6e62-10ec-4b0c-9bce-81931a36f9b4', 'd8cdc0fe-9e00-4790-a1fb-5fddb84ed845', 'PUSH',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('5108ecff-adfb-46d2-bbbf-2e333b08a738', 'd8cdc0fe-9e00-4790-a1fb-5fddb84ed845', 'EMAIL',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('2460bb80-8fcf-497d-9db1-cf8da918c52b', 'd8cdc0fe-9e00-4790-a1fb-5fddb84ed845', 'SMS',
        '',
        false);

-- Заявка отмена Автором
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('8a7ce94d-1b22-4738-b5e8-a7c127fd9906', 'REQUEST_CARGO', 'Заявка отмена Автором', 'notice_813',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'APPROVE', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('c998f8d8-b9a2-4aae-947d-f2fc319b893c', '8a7ce94d-1b22-4738-b5e8-a7c127fd9906', 'PUSH',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('a637f5b9-51ef-4c9d-8faa-3aa51fa3447f', '8a7ce94d-1b22-4738-b5e8-a7c127fd9906', 'EMAIL',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('2c958b99-2829-42ed-b293-13fb29dd7409', '8a7ce94d-1b22-4738-b5e8-a7c127fd9906', 'SMS',
        '',
        false);

-- Заявка отменена Инженером
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('54d71655-dc81-4c8f-b605-1f13c7cc836d', 'REQUEST_CARGO', 'Заявка отменена Инженером', 'notice_814',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_CANCELED_BY_ENGINEER', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('f2d1956b-64a3-4cfe-930a-851735504d74', '54d71655-dc81-4c8f-b605-1f13c7cc836d', 'PUSH',
        'Заявка {humanReadableId} отменена Инженером',
        true);

INSERT INTO notifications_settings.channel
VALUES ('156413f5-e724-41e3-b5e1-141763062284', '54d71655-dc81-4c8f-b605-1f13c7cc836d', 'EMAIL',
        'Заявка {humanReadableId} отменена Инженером',
        false);

INSERT INTO notifications_settings.channel
VALUES ('b44c01db-e6de-46a0-a498-93d658301034', '54d71655-dc81-4c8f-b605-1f13c7cc836d', 'SMS',
        '',
        false);


INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
VALUES ('b44c01db-e6de-46a0-a498-93d658301034', 0, 'AT_EVENT', '54d71655-dc81-4c8f-b605-1f13c7cc836d', null, null);

-- Заявка отменена контрагентом
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('5ebb72ed-7c11-4892-afbe-503ea26e2d3a', 'REQUEST_CARGO', 'Заявка отменена контрагентом', 'notice_815',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_CANCELED_BY_CONTRACTOR', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('77da8263-09cd-4d4c-9e50-b6708100e8b2', '5ebb72ed-7c11-4892-afbe-503ea26e2d3a', 'PUSH',
        'Заявка {humanReadableId} отменена контрагентом',
        true);

INSERT INTO notifications_settings.channel
VALUES ('be4fe761-3c55-4e6f-b87d-dec30eb66d1e', '5ebb72ed-7c11-4892-afbe-503ea26e2d3a', 'EMAIL',
        'Заявка {humanReadableId} отменена контрагентом',
        false);

INSERT INTO notifications_settings.channel
VALUES ('4a4ce048-b925-4be6-9d79-dc85bf5288b9', '5ebb72ed-7c11-4892-afbe-503ea26e2d3a', 'SMS',
        '',
        false);

INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
VALUES ('4a4ce048-b925-4be6-9d79-dc85bf5288b9', 0, 'AT_EVENT', '5ebb72ed-7c11-4892-afbe-503ea26e2d3a', null, null);


-- Маршрут отменён контрагентом
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('8b6e008f-c358-4b4a-8551-964338abce45', 'REQUEST_CARGO', 'Маршрут отменён контрагентом', 'notice_816',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'CARGO_CANCELED_BY_CONTRACTOR', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES ('51e14df1-e365-4df4-b812-f69d2f17319c', '8b6e008f-c358-4b4a-8551-964338abce45', 'PUSH',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('844c3eea-fc13-46dc-aec3-0a12f1f47215', '8b6e008f-c358-4b4a-8551-964338abce45', 'EMAIL',
        '',
        false);

INSERT INTO notifications_settings.channel
VALUES ('521e71f3-4e8b-4e05-a48e-3f0bd870431a', '8b6e008f-c358-4b4a-8551-964338abce45', 'SMS',
        '',
        false);