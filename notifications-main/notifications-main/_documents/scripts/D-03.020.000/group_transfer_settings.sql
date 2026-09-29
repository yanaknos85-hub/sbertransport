do $$
declare
    org_id uuid;
    settings_uuid uuid;
begin

    settings_uuid = gen_random_uuid();
    org_id = :oganizationId::uuid;
    RAISE NOTICE'Value of settings_uuid %', settings_uuid;
    RAISE NOTICE'Value of org_id %', org_id;
-- Настройки смс-уведомления клиента Манжерок при назначении водителя для трансфера за час до отправки
INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                 parent_type)
VALUES (settings_uuid, 'REQUEST_GROUP_TRANSFER', 'Назначении водителя', 'notice_2101',
        org_id, null, 'GROUP_TRANSFER_DRIVER_FOUND', null, 'ORGANIZATION');

INSERT INTO notifications_settings.channel
VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
        'Добрый день! По трансферу {localDesiredDate} на {localDesiredTime} с {departureAddress} до {destinationAddress} Назначен автомобиль: {carInfo} Водитель {driverFio}',
        false);

INSERT INTO notifications_settings.channel
VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
        'Добрый день! По трансферу {localDesiredDate} на {localDesiredTime} с {departureAddress} до {destinationAddress} Назначен автомобиль: {carInfo} Водитель {driverFio}',
        false);

INSERT INTO notifications_settings.channel
VALUES (gen_random_uuid(), settings_uuid, 'SMS',
        'Добрый день! По трансферу {localDesiredDate} на {localDesiredTime} с {departureAddress} до {destinationAddress} Назначен автомобиль: {carInfo} Водитель {driverFio}',
        true);

INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
VALUES (gen_random_uuid(), 600000000000, 'BEFORE_DEADLINE', settings_uuid, 'desiredDate', null);

    RAISE NOTICE'Для отката выполните:';
    RAISE NOTICE'delete from notifications.notification nn where  nn.settings_id = %', settings_uuid;
    RAISE NOTICE'delete from notifications_settings.channel where settings_id  = %', settings_uuid;
    RAISE NOTICE'delete from notifications_settings.send_time where settings_id = %', settings_uuid;
    RAISE NOTICE'delete from notifications_settings.send_count where settings_id = %', settings_uuid;
    RAISE NOTICE'delete from notifications_settings.notification where id = %', settings_uuid;

end$$;