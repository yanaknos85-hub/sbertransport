create or replace function notifications_settings.create_or_update_notification_channel (
	p_channel notifications_settings.channel.channel%type,
	p_settings_id notifications_settings.channel.settings_id%type,
	p_text notifications_settings.channel.text%type,
	p_active notifications_settings.channel.active%type
) RETURNS notifications_settings.channel.id%type
LANGUAGE plpgsql
AS $$
DECLARE
    v_channel_id notifications_settings.channel.id%type;
begin
	select id into v_channel_id from notifications_settings.channel
	where settings_id = p_settings_id and channel = p_channel;
    if v_channel_id is null then
        v_channel_id := gen_random_uuid();
        insert into notifications_settings.channel (id, settings_id, channel, text, active)
        values (v_channel_id, p_settings_id, p_channel, p_text, p_active);
        raise NOTICE'Channel added for settings_id %, channel % ', p_settings_id, p_channel;
    ELSE
        update notifications_settings.channel
        set text = p_text, active = p_active where id = v_channel_id;
        RAISE NOTICE'Channel updated for settings_id %, channel % ', p_settings_id, p_channel;
    END IF;
	return v_channel_id;
END
$$;

create or replace function notifications_settings.update_notification (
    p_settings_id notifications_settings.notification.id%type,
	p_name notifications_settings.notification.name%type,
	p_description notifications_settings.notification.description%type,
	p_text notifications_settings.notification.text%type
) RETURNS notifications_settings.notification.id%type
LANGUAGE plpgsql
AS $$
DECLARE
    v_settings_id notifications_settings.notification.id%type;
begin

    select id into v_settings_id from notifications_settings.notification
    where id = p_settings_id;

    IF v_settings_id is null then
        raise NOTICE'Settings not found for settings_id % ', v_settings_id;
    ELSE
        update notifications_settings.notification set name = p_name, description = p_description, text = p_text where id = v_settings_id;
        RAISE NOTICE'Notification updated for settings_id % with values (name = %, description = %, text = %', v_settings_id, p_name, p_description, p_text;
    END IF;
	return v_settings_id;
END
$$;

create or replace procedure notifications_settings.update_all_notification_and_channels (
	p_class notifications_settings.notification.class%type,
	p_type notifications_settings.notification.type%type,
	p_name notifications_settings.notification.name%type,
	p_description notifications_settings.notification.description%type,
	p_text notifications_settings.notification.text%type,
	p_text_channel notifications_settings.channel.text%type,
	p_push_active notifications_settings.channel.active%type,
	p_sms_active notifications_settings.channel.active%type,
	p_email_active notifications_settings.channel.active%type
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_rec RECORD;
    v_settings_id notifications_settings.notification.id%type;
    v_channel_id notifications_settings.channel.id%type;
    v_updated integer := 0;
begin

    FOR v_rec IN select id, "class", "type", "description" from notifications_settings.notification where "class" = p_class and "type" = p_type  LOOP
        select notifications_settings.update_notification (
            p_settings_id => v_rec.id,
            p_name => p_name,
            p_description => p_description,
            p_text => p_text)
        into v_settings_id;

        v_updated := v_updated + 1;

        select notifications_settings.create_or_update_notification_channel (
            p_channel => 'PUSH'::varchar,
            p_settings_id => v_settings_id,
            p_text => p_text_channel,
            p_active => p_push_active
        ) into v_channel_id;

        select notifications_settings.create_or_update_notification_channel (
            p_channel => 'SMS'::varchar,
            p_settings_id => v_settings_id,
            p_text => p_text_channel,
            p_active => p_sms_active
        ) into v_channel_id;

        select notifications_settings.create_or_update_notification_channel (
            p_channel => 'EMAIL'::varchar,
            p_settings_id => v_settings_id,
            p_text => p_text_channel,
            p_active => p_email_active
        ) into v_channel_id;
    END LOOP;

    RAISE NOTICE'Updated % notifications for p_class = % and p_type = %', v_updated, p_class, p_type;
end
$$;

create or replace procedure notifications_settings.delete_unused_notification_settings (
    p_class notifications_settings.notification.class%TYPE,
	p_type 	notifications_settings.notification.type%TYPE
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_rec RECORD;
	v_num_rows integer := 0;
	v_count integer := 0;
begin

	select count(1) from notifications.notification
    join notifications_settings.notification s on s.id = settings_id
    into v_count where "class" = p_class and type = p_type;

	IF v_count > 0
	THEN
		raise NOTICE 'Settings not deleted, has % notifications', v_count;
		return;
	END IF;
    FOR v_rec IN select id, "class", "type" from notifications_settings.notification where "class" = p_class and "type" = p_type  LOOP

--		raise NOTICE 'Clear settings id = %', v_rec.id;

		delete from notifications_settings.user_notification_settings where notification_id = v_rec.id;
--	    GET DIAGNOSTICS v_num_rows = ROW_COUNT;
--	    raise NOTICE 'Deleted % user settings', v_num_rows;

		delete from notifications_settings.send_count where settings_id = v_rec.id;
--		GET DIAGNOSTICS v_num_rows = ROW_COUNT;
--	    raise NOTICE 'Deleted % send_counts', v_num_rows;

		delete from notifications_settings.send_time where settings_id = v_rec.id;
--		GET DIAGNOSTICS v_num_rows = ROW_COUNT;
--	    raise NOTICE 'Deleted % send_times', v_num_rows;

		delete from notifications_settings.channel where settings_id = v_rec.id;
--		GET DIAGNOSTICS v_num_rows = ROW_COUNT;
--	    raise NOTICE 'Deleted % channels', v_num_rows;
    END LOOP;

	delete from notifications_settings.notification where "class" = p_class and "type" = p_type;
	GET DIAGNOSTICS v_num_rows = ROW_COUNT;
    raise NOTICE 'Deleted % notification for  class = %, type = %', v_num_rows, p_class, p_type;

end
$$;


create or replace procedure notifications_settings.update_send_time_for (
	p_class notifications_settings.notification.class%type,
	p_type notifications_settings.notification.type%type,
	p_time_before notifications_settings.send_time.time_before%type,
    p_type_send_time notifications_settings.send_time.type%type,
    p_field_name notifications_settings.send_time.field_name%type,
    p_deadline_field_name notifications_settings.send_time.deadline_field_name%type
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_rec RECORD;
    v_updated integer := 0;
begin

    FOR v_rec IN select id, "class", "type", "description" from notifications_settings.notification where "class" = p_class and "type" = p_type  LOOP

        delete from notifications_settings.send_time
        where settings_id = v_rec.id;

        delete from notifications_settings.send_count
        where settings_id = v_rec.id;

        insert into notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
        values (gen_random_uuid(), p_time_before, p_type_send_time, v_rec.id, p_field_name, p_deadline_field_name);
	
		v_updated := v_updated + 1;

    END LOOP;

    RAISE NOTICE'Updated % notifications for p_class = % and p_type = %', v_updated, p_class, p_type;
end
$$;