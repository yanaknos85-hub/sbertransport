update notifications_settings.channel
set text = 'По вашей поездке {request.humanReadableId} завершен этап "Согласование" со статусом "{approveStatusDescription}".'
where text in (
    select distinct text from notifications_settings.channel where text ilike '%завершен этап "Согласование" со статусом %'
    );

update notifications_settings.channel
set text = 'По вашей поездке {request.humanReadableId} завершен этап "Согласование" со статусом "{approveStatusDescription}".'
where text in (
    select distinct text from notifications_settings.channel where text ilike '%завершен этап ''Согласование''%'
    );

update notifications_settings.channel
set text = 'Заказ выполнит {driver.lastName} {driver.firstName} {driver.patronymic}, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.'
where text = 'Заказ выполнит {taxiTrip.driver.lastName} {taxiTrip.driver.firstName} {taxiTrip.driver.patronymic}, автомобиль {taxiTrip.vehicle.color} {taxiTrip.vehicle.brand} {taxiTrip.vehicle.model} {taxiTrip.vehicle.stateNumber}.'