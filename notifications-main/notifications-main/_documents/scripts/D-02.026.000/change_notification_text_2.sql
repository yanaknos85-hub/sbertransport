update notifications_settings.channel set text = 'Ваша заявка {humanReadableId} была отправлена на выплату.' where text in (
    select distinct text from notifications_settings.channel where text ilike '%была отправлена на выплату%'
);