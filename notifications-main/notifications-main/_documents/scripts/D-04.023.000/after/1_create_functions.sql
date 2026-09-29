create or replace function notifications_settings.create_or_update_notification (
	p_class notifications_settings.notification.class%type,
	p_name notifications_settings.notification.name%type,
	p_description notifications_settings.notification.description%type,
	p_parent_id notifications_settings.notification.parent_id%type,
	p_owner_id notifications_settings.notification.owner_id%type,
	p_type notifications_settings.notification.type%type,
	p_text notifications_settings.notification.text%type,
	p_parent_type notifications_settings.notification.parent_type%type
) RETURNS notifications_settings.notification.id%type
LANGUAGE plpgsql
AS $$
DECLARE
    v_settings_id notifications_settings.notification.id%type;
begin
	if p_owner_id is null then
	    select id into v_settings_id from notifications_settings.notification
        where class = p_class and type = p_type and parent_type = p_parent_type and parent_id = p_parent_id and owner_id is null;
	else
        select id into v_settings_id from notifications_settings.notification
        where class = p_class and type = p_type and parent_type = p_parent_type and parent_id = p_parent_id and owner_id = p_owner_id;
	end if;


    if v_settings_id is null then
        v_settings_id := gen_random_uuid();
        insert into notifications_settings.notification (id, class, name, description, parent_id, owner_id, type, text, parent_type)
        values (v_settings_id, p_class, p_name, p_description, p_parent_id, p_owner_id, p_type, p_text, p_parent_type);
        raise NOTICE'Notification added for class %, type %, parent_type %, parent_id % ', p_class, p_type, p_parent_type, p_parent_id;
    ELSE
        update notifications_settings.notification set name = p_name, description = p_description, text = p_text where id = v_settings_id;
        RAISE NOTICE'Notification updated for class %, type %, parent_type %, parent_id % ', p_class, p_type, p_parent_type, p_parent_id;
    END IF;
	return v_settings_id;
END
$$;

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
        raise NOTICE'Channel added for settings_id %, channel % ', p_settings_id, p_channel;
        v_channel_id := gen_random_uuid();
        insert into notifications_settings.channel (id, settings_id, channel, text, active)
        values (v_channel_id, p_settings_id, p_channel, p_text, p_active);
    ELSE
        update notifications_settings.channel
        set text = p_text, active = p_active where id = v_channel_id;
        RAISE NOTICE'Channel updated for settings_id %, channel % ', p_settings_id, p_channel;
    END IF;
	return v_channel_id;
END
$$;

create or replace function notifications_settings.create_or_update_notification_send_time (
    p_time_before notifications_settings.send_time.time_before%type,
    p_type notifications_settings.send_time.type%type,
    p_settings_id notifications_settings.send_time.settings_id%type,
    p_field_name notifications_settings.send_time.field_name%type,
    p_deadline_field_name notifications_settings.send_time.deadline_field_name%type
) RETURNS notifications_settings.send_time.id%type
LANGUAGE plpgsql
AS $$
DECLARE
    v_id notifications_settings.send_time.id%type;
begin
	select id into v_id from notifications_settings.send_time
	where settings_id = p_settings_id and type = p_type;
    if v_id is null then
        raise NOTICE'send_time added for settings_id %, type %, id % ', p_settings_id, p_type, v_id;
        v_id := gen_random_uuid();
        insert into notifications_settings.send_time (id, time_before, type, settings_id, field_name, deadline_field_name)
        values (v_id, p_time_before, p_type, p_settings_id, p_field_name, p_deadline_field_name);
    ELSE
        update notifications_settings.send_time
        set time_before = p_time_before, type = p_type, field_name = p_field_name, deadline_field_name = p_deadline_field_name
        where id = v_id;
        RAISE NOTICE'send_time updated for settings_id %, type %, id % ', p_settings_id, p_type, v_id;
    END IF;
	return v_id;
END
$$;

create or replace function notifications_settings.create_or_update_notification_and_channels (
	p_class notifications_settings.notification.class%type,
	p_name notifications_settings.notification.name%type,
	p_description notifications_settings.notification.description%type,
	p_parent_id notifications_settings.notification.parent_id%type,
	p_owner_id notifications_settings.notification.owner_id%type,
	p_type notifications_settings.notification.type%type,
	p_text notifications_settings.notification.text%type,
	p_parent_type notifications_settings.notification.parent_type%type,
	p_text_channel notifications_settings.channel.text%type,
	p_push_active notifications_settings.channel.active%type,
	p_sms_active notifications_settings.channel.active%type,
	p_email_active notifications_settings.channel.active%type
) RETURNS notifications_settings.notification.id%type
LANGUAGE plpgsql
AS $$
DECLARE
    v_settings_id notifications_settings.notification.id%type;
    v_channel_id notifications_settings.channel.id%type;
begin

    select notifications_settings.create_or_update_notification (
        p_class => p_class,
        p_name => p_name,
        p_description => p_description,
        p_parent_id => p_parent_id,
        p_owner_id => p_owner_id,
        p_type => p_type,
        p_text => p_text,
        p_parent_type => p_parent_type)
	into v_settings_id;

    delete from notifications_settings.channel where settings_id  = v_settings_id;

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

	return v_settings_id;
end
$$;

create or replace function notifications_settings.create_or_update_notification_with_send_time (
	p_class notifications_settings.notification.class%type,
	p_name notifications_settings.notification.name%type,
	p_description notifications_settings.notification.description%type,
	p_parent_id notifications_settings.notification.parent_id%type,
	p_owner_id notifications_settings.notification.owner_id%type,
	p_type notifications_settings.notification.type%type,
	p_text notifications_settings.notification.text%type,
	p_parent_type notifications_settings.notification.parent_type%type,
	p_text_channel notifications_settings.channel.text%type,
	p_push_active notifications_settings.channel.active%type,
	p_sms_active notifications_settings.channel.active%type,
	p_email_active notifications_settings.channel.active%type,
    p_time_before notifications_settings.send_time.time_before%type,
    p_type_send_time notifications_settings.send_time.type%type,
    p_field_name notifications_settings.send_time.field_name%type,
    p_deadline_field_name notifications_settings.send_time.deadline_field_name%type
) RETURNS notifications_settings.notification.id%type
LANGUAGE plpgsql
AS $$
DECLARE
    v_settings_id notifications_settings.notification.id%type;
    v_send_time_id notifications_settings.send_time.id%type;
begin
    raise NOTICE'-----Start adding notifications with class %, type % to parent_id % -----', p_class, p_type, p_parent_id;
     select notifications_settings.create_or_update_notification_and_channels (
        p_class => p_class,
        p_name => p_name,
        p_description => p_description,
        p_parent_id => p_parent_id,
        p_owner_id => p_owner_id,
        p_type => p_type,
        p_text => p_text,
        p_parent_type => p_parent_type,
        p_text_channel => p_text_channel,
        p_push_active => p_push_active,
        p_sms_active => p_sms_active,
        p_email_active => p_email_active)
	 into v_settings_id;

    delete from notifications_settings.send_time where settings_id = v_settings_id;
    delete from notifications_settings.send_count where settings_id = v_settings_id;

	select notifications_settings.create_or_update_notification_send_time (
        p_time_before => p_time_before,
        p_type => p_type_send_time,
        p_settings_id => v_settings_id,
        p_field_name => p_field_name,
        p_deadline_field_name => p_deadline_field_name
	) into v_send_time_id;

    RAISE NOTICE'-----Notifications added successfully %, class %, type % -----', v_settings_id, p_class, p_type;
	return v_settings_id;
END
$$;

