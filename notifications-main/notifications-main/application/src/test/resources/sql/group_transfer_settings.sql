-- Назначен водитель на групповой трансфер
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES ('73df9eb7-5b75-498f-9be7-aef283bfabdd', 'REQUEST_GROUP_TRANSFER', 'Согласование заявки', 'notice_901',
        'ac1d7c0e-6ebd-40b4-9595-086dbed05c22', null, 'GROUP_TRANSFER_DRIVER_FOUND_MZK', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES (gen_random_uuid(), '73df9eb7-5b75-498f-9be7-aef283bfabdd', 'PUSH',
        'Добрый день! По трансферу {localDesiredDate} на {localDesiredTime} с {departureAddress} до {destinationAddress} Назначен автомобиль: {carInfo} Водитель {authorFio}',
        false);

INSERT INTO notifications_settings.channel
VALUES (gen_random_uuid(), '73df9eb7-5b75-498f-9be7-aef283bfabdd', 'EMAIL',
        'Добрый день! По трансферу {localDesiredDate} на {localDesiredTime} с {departureAddress} до {destinationAddress} Назначен автомобиль: {carInfo} Водитель {authorFio}',
        false);

INSERT INTO notifications_settings.channel
VALUES (gen_random_uuid(), '73df9eb7-5b75-498f-9be7-aef283bfabdd', 'SMS',
        'Добрый день! По трансферу {localDesiredDate} на {localDesiredTime} с {departureAddress} до {destinationAddress} Назначен автомобиль: {carInfo} Водитель {driverFio}',
        true);

INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
VALUES (gen_random_uuid(), 10, 'BEFORE_DEADLINE', '73df9eb7-5b75-498f-9be7-aef283bfabdd', 'desiredDate', null);
