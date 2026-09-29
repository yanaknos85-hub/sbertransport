update notifications_settings.channel set text = 'Заказ выполнит {driver.lastName} {driver.firstName} {driver.patronymic}, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.'
where id in(
    select id from notifications_settings.channel
    where text ilike 'Заказ выполнит%');

update notifications_settings.channel set text = 'Водитель {driver.lastName} {driver.firstName} {driver.patronymic} ожидает в пункте назначения, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.'
where id in(
    select id from notifications_settings.channel
    where text ilike '%ожидает в пункте назначения%');

update notifications_settings.channel set text = 'Заказ выполнит {driver.lastName} {driver.firstName} {driver.patronymic}, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.'
where id in (select id from notifications_settings.channel where settings_id in
                                                                 (select id from notifications_settings.notification where description = 'notice_104_disp' or description = 'notice_104'));