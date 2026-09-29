update notifications_settings.channel
set text = 'Водитель {driver.lastname} {driver.firstname} {driver.patronymic} ожидает в пункте назначения, автомобиль - {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.registrationNumber}.'
where settings_id in (
    select id from notifications_settings.notification where  type = 'DRIVER_ARRIVED'
);