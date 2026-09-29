delete from notifications_settings.channel where settings_id in (
    select id
    from (
             select id, row_number() over (partition by class, parent_id, type, description order by id) as rowNumber
             from notifications_settings.notification
         ) as duplicate
    where duplicate.rowNumber > 1
);

delete from notifications_settings.send_count where settings_id in (
    select id
    from (
             select id, row_number() over (partition by class, parent_id, type, description order by id) as rowNumber
             from notifications_settings.notification
         ) as duplicate
    where duplicate.rowNumber > 1
);

delete from notifications_settings.send_time where settings_id in (
    select id
    from (
             select id, row_number() over (partition by class, parent_id, type, description order by id) as rowNumber
             from notifications_settings.notification
         ) as duplicate
    where duplicate.rowNumber > 1
);

delete from notifications.notification where settings_id in (
    select id
    from (
             select id, row_number() over (partition by class, parent_id, type, description order by id) as rowNumber
             from notifications_settings.notification
         ) as duplicate
    where duplicate.rowNumber > 1
);

delete from notifications_settings.notification where id in (
    select id
    from (
             select id, row_number() over (partition by class, parent_id, type, description order by id) as rowNumber
             from notifications_settings.notification
         ) as duplicate
    where duplicate.rowNumber > 1
)