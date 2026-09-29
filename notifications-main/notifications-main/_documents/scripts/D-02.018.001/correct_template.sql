update notifications_settings.channel
set text = 'Заказ выполнит {driver.lastName} {driver.firstName} {driver.patronymic}, автомобиль - {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.registrationNumber}.'
where text ilike '%vehicle.stateNumber%';
update notifications_settings.channel
set text = 'Заказ будет выполнен. {resolution}.'
where text ilike '%taxiTrip.resolution%';