do
$$
    declare
        org_id        uuid;
        settings_uuid uuid;
    begin


        org_id = :oganizationId::uuid;
        RAISE NOTICE 'Value of org_id %', org_id;

-- Согласование заявки
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3001: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Согласование заявки', 'notice_3001',
                org_id, null, 'APPROVE', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                'На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}',
                true);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                'На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                'На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}',
                false);

        INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
        VALUES (gen_random_uuid(), 0, 'AT_EVENT', settings_uuid, null,
                null);

-- Статус согласования
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3002: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Статус согласования', 'notice_3002',
                org_id, null, 'APPROVE_STATUS', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                'Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}',
                true);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                'Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                'Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}',
                false);

        INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
        VALUES (gen_random_uuid(), 0, 'AT_EVENT', settings_uuid, null,
                null);

-- Формирование маршрута
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3003: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Формирование маршрута', 'notice_3003',
                org_id, null, 'CARGO_PLANNING', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

-- Маршрут направлен контрагенту
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3004: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Маршрут направлен контрагенту', 'notice_3004',
                org_id, null, 'CARGO_PLANNING', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

-- Назначение водителя/курьера на маршрут
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3005: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Назначение водителя/курьера на маршрут',
                'notice_3005',
                org_id, null, 'CARGO_PLANNING', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

-- Назначение водителя на заявку
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3006: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Назначение водителя/курьера на заявку',
                'notice_3006',
                org_id, null, 'CARGO_DRIVER_AWAITING_DATA', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                'На заявку {humanReadableId} назначен водитель {authorFio} и автомобиль {carInfo}',
                true);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                'На заявку {humanReadableId} назначен водитель {authorFio} и автомобиль {carInfo}',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

        INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
        VALUES (gen_random_uuid(), 0, 'AT_EVENT', settings_uuid, null,
                null);

-- Назначение курьера на заявку
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3017: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Назначение водителя/курьера на заявку',
                'notice_3017',
                org_id, null, 'CARGO_COURIER_AWAITING_DATA', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                'На заявку {humanReadableId} назначен курьер',
                true);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                'На заявку {humanReadableId} назначен курьер',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

        INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
        VALUES (gen_random_uuid(), 0, 'AT_EVENT', settings_uuid, null,
                null);


-- Водитель/курьер выехал на маршрут
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3007: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Водитель/курьер выехал на маршрут',
                'notice_3007',
                org_id, null, 'CARGO_AWAITING_TRANSFER', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

        INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
        VALUES (gen_random_uuid(), 0, 'AT_EVENT', settings_uuid, null,
                null);


-- Водитель/курьер на точке загрузки
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3008: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Водитель/курьер на точке загрузки',
                'notice_3008',
                org_id, null, 'CARGO_AWAITING_TRANSFER', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

-- Водитель/курьер на точке разгрузки
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3009: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Водитель/курьер на точке разгрузки',
                'notice_3009',
                org_id, null, 'CARGO_TRANSFER_FINISHED', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

-- Поездка завершена
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3010: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Поездка завершена', 'notice_3010',
                org_id, null, 'CARGO_DELIVERY_CONFIRMATION_FINISHED', null,
                'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

-- Заявка исполнена - оценка
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3011: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Заявка исполнена - оценка', 'notice_3011',
                org_id, null, 'CARGO_SHIPMENT_FINISHED', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                'Заявка {humanReadableId} на грузоперевозку исполнена. Оцените качество предоставленных услуг',
                true);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                'Заявка {humanReadableId} на грузоперевозку исполнена. Оцените качество предоставленных услуг',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                'Заявка {humanReadableId} на грузоперевозку исполнена. Оцените качество предоставленных услуг',
                false);


        INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
        VALUES (gen_random_uuid(), 0, 'AT_EVENT', settings_uuid, null,
                null);

-- Поездка завершена
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3012: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Поездка завершена', 'notice_3012',
                org_id, null, 'APPROVE', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

-- Заявка отмена Автором
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3013: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Заявка отмена Автором', 'notice_3013',
                org_id, null, 'APPROVE', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

-- Заявка отменена Инженером
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3014: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Заявка отменена Инженером', 'notice_3014',
                org_id, null, 'CARGO_CANCELED_BY_ENGINEER', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                'Заявка {humanReadableId} отменена Инженером',
                true);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                'Заявка {humanReadableId} отменена Инженером',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);


        INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
        VALUES (gen_random_uuid(), 0, 'AT_EVENT', settings_uuid, null,
                null);

-- Заявка отменена контрагентом
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3015: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Заявка отменена контрагентом', 'notice_3015',
                org_id, null, 'CARGO_CANCELED_BY_CONTRACTOR', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                'Заявка {humanReadableId} отменена контрагентом',
                true);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                'Заявка {humanReadableId} отменена контрагентом',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

        INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
        VALUES (gen_random_uuid(), 0, 'AT_EVENT', settings_uuid, null,
                null);


-- Маршрут отменён контрагентом
        settings_uuid = gen_random_uuid();
        RAISE NOTICE 'notice_3016: Value of settings_uuid %', settings_uuid;
        INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text,
                                                         parent_type)
        VALUES (settings_uuid, 'REQUEST_CARGO', 'Маршрут отменён контрагентом', 'notice_3016',
                org_id, null, 'CARGO_CANCELED_BY_CONTRACTOR', null, 'ORGANIZATION');

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                '',
                false);

        INSERT INTO notifications_settings.channel
        VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                '',
                false);

        RAISE NOTICE 'Для отката выполните:';
        RAISE NOTICE 'delete from notifications.notification nn where  nn.settings_id = settings_uuid_для_noticexxxx';
        RAISE NOTICE 'delete from notifications_settings.channel where settings_id  = settings_uuid_для_noticexxxx';
        RAISE NOTICE 'delete from notifications_settings.send_time where settings_id = settings_uuid_для_noticexxxx';
        RAISE NOTICE 'delete from notifications_settings.send_count where settings_id = settings_uuid_для_noticexxxx';
        RAISE NOTICE 'delete from notifications_settings.notification where id = settings_uuid_для_noticexxxx';

    end
$$;