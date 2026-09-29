--в паре с settings.sql

insert into notifications_settings.user_notification_settings
(id, user_id, notification_id, "class", "name", push_active, push_enabled, email_active, email_enabled, sms_active, sms_enabled, parent_id)
values(gen_random_uuid(),
       '91211794-216d-4b92-9ae9-2aeea94e0d2d',
       '00000000-0000-0000-0000-000000000000',
       'REQUEST_CARGO',
       'Согласование заявки', true, true, true, true, true, true, 'ac1d7c0e-6ebd-40b4-9595-086dbed05c22');

