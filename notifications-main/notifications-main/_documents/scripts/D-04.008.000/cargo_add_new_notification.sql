do
$$
    declare
        settings_uuid uuid;
        org_id        uuid;
        _r            record;
    begin
        for _r in
            select distinct n.parent_id
            from notifications_settings.notification n
            where n.parent_type = 'ORGANIZATION'
              and class = 'REQUEST_CARGO'
              and type != 'CARGO_CHANGE_DESIRED_DATE'
              and n.parent_id not in (select distinct parent_id from notifications_settings.notification where type = 'CARGO_CHANGE_DESIRED_DATE')
            loop
                org_id = _r.parent_id;
                settings_uuid = gen_random_uuid();
                RAISE NOTICE 'Value of org_id %', org_id;
                RAISE NOTICE 'notice_818: Value of settings_uuid %', settings_uuid;

                INSERT INTO notifications_settings.notification (id, class, name, description, parent_id, owner_id,
                                                                 type, text,
                                                                 parent_type)
                VALUES (settings_uuid, 'REQUEST_CARGO', 'Изменение плановых сроков исполнения', 'notice_818',
                        org_id, null, 'CARGO_CHANGE_DESIRED_DATE', null, 'ORGANIZATION');

                INSERT INTO notifications_settings.channel
                VALUES (gen_random_uuid(), settings_uuid, 'PUSH',
                        'По заявке {humanReadableId} изменен плановый срок исполнения на {desiredDate}',
                        true);

                INSERT INTO notifications_settings.channel
                VALUES (gen_random_uuid(), settings_uuid, 'EMAIL',
                        'По заявке {humanReadableId} изменен плановый срок исполнения на {desiredDate}',
                        true);

                INSERT INTO notifications_settings.channel
                VALUES (gen_random_uuid(), settings_uuid, 'SMS',
                        'По заявке {humanReadableId} изменен плановый срок исполнения на {desiredDate}',
                        true);

                INSERT INTO notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
                VALUES (gen_random_uuid(), 0, 'AT_EVENT', settings_uuid, null,
                        null);

                RAISE NOTICE 'Для отката выполните:';
                RAISE NOTICE 'delete from notifications.notification nn where  nn.settings_id = settings_uuid_для_noticexxxx';
                RAISE NOTICE 'delete from notifications_settings.channel where settings_id  = settings_uuid_для_noticexxxx';
                RAISE NOTICE 'delete from notifications_settings.send_time where settings_id = settings_uuid_для_noticexxxx';
                RAISE NOTICE 'delete from notifications_settings.send_count where settings_id = settings_uuid_для_noticexxxx';
                RAISE NOTICE 'delete from notifications_settings.user_notification_settings where notification_id = settings_uuid_для_noticexxxx';
                RAISE NOTICE 'delete from notifications_settings.notification where id = settings_uuid_для_noticexxxx';
            end loop;
    end
$$;